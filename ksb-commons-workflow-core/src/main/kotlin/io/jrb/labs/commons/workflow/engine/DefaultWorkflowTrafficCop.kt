/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Jon Brule
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.jrb.labs.commons.workflow.engine

import io.jrb.labs.commons.eventbus.Event
import io.jrb.labs.commons.workflow.api.OutcomeResolution
import io.jrb.labs.commons.workflow.api.RecordedStepResult
import io.jrb.labs.commons.workflow.api.RoutedEvent
import io.jrb.labs.commons.workflow.api.StepResult
import io.jrb.labs.commons.workflow.api.StepResult.Errored
import io.jrb.labs.commons.workflow.api.StepResult.Failed
import io.jrb.labs.commons.workflow.api.StepResult.Ignored
import io.jrb.labs.commons.workflow.api.StepResult.Success
import io.jrb.labs.commons.workflow.api.StepResult.Waiting
import io.jrb.labs.commons.workflow.api.WorkflowContext
import io.jrb.labs.commons.workflow.api.WorkflowDefinition
import io.jrb.labs.commons.workflow.api.WorkflowEvent
import io.jrb.labs.commons.workflow.api.WorkflowEventEnvelope
import io.jrb.labs.commons.workflow.api.WorkflowFailedEvent
import io.jrb.labs.commons.workflow.api.WorkflowFailureDetails
import io.jrb.labs.commons.workflow.api.WorkflowHistoryEntry
import io.jrb.labs.commons.workflow.api.WorkflowInstance
import io.jrb.labs.commons.workflow.api.WorkflowRegistry
import io.jrb.labs.commons.workflow.api.WorkflowStatus
import io.jrb.labs.commons.workflow.api.WorkflowTransition
import io.jrb.labs.commons.workflow.api.workflowEnvelope
import io.jrb.labs.commons.workflow.api.workflowPayload
import io.jrb.labs.commons.workflow.spi.WorkflowEventPublisher
import io.jrb.labs.commons.workflow.spi.WorkflowInstanceStore
import java.time.Instant

class DefaultWorkflowTrafficCop(
    private val workflowRegistry: WorkflowRegistry,
    private val instanceStore: WorkflowInstanceStore,
    private val transitionMatcher: TransitionMatcher,
    private val stepExecutor: StepExecutor,
    private val eventPublisher: WorkflowEventPublisher
) : WorkflowTrafficCop {

    override suspend fun handleEvent(event: Event) {
        val envelope = event.workflowEnvelope()

        if (envelope != null) {
            handleWorkflowEnvelope(envelope)
            return
        }

        val explicitInstance = (event as? WorkflowEvent)
            ?.workflowInstanceId
            ?.let { instanceStore.findByInstanceId(it) }

        if (explicitInstance != null) {
            advanceExistingWorkflow(explicitInstance, event)
            return
        }

        val primedDefinitions = workflowRegistry.findByPrimingEvent(event)

        primedDefinitions.forEach { definition ->
            startWorkflow(definition, event)
        }
    }

    private suspend fun handleWorkflowEnvelope(envelope: WorkflowEventEnvelope<*>) {
        val instance = instanceStore.findByInstanceId(envelope.workflowInstanceId)
            ?: return

        if (instance.workflowName != envelope.workflowName) {
            return
        }

        advanceExistingWorkflow(instance, envelope)
    }

    private suspend fun startWorkflow(
        definition: WorkflowDefinition,
        event: Event
    ) {
        val correlationId = definition.correlationIdOf(event)
        val requestId = definition.requestIdOf(event)

        val instance = WorkflowInstance(
            workflowName = definition.name,
            correlationId = correlationId,
            state = definition.initialState,
            status = WorkflowStatus.RUNNING,
            context = WorkflowContext(
                attributes = buildMap {
                    requestId?.let { put("requestId", it) }
                }
            )
        )

        instanceStore.save(instance)

        advanceExistingWorkflow(instance, event)
    }

    private suspend fun advanceExistingWorkflow(
        instance: WorkflowInstance,
        event: Event
    ) {
        val definition = workflowRegistry.definitions()
            .firstOrNull { it.name == instance.workflowName }
            ?: return

        val inboundEvent = event.workflowPayload()

        val transition = transitionMatcher.findMatchingTransition(
            definition = definition,
            currentState = instance.state,
            event = inboundEvent
        ) ?: return

        val execution = executeTransition(
            instance = instance,
            inboundEvent = inboundEvent,
            transition = transition
        )

        val recorded = RecordedStepResult(
            stepName = execution.stepName,
            outcomeType = execution.result::class.simpleName ?: "Unknown",
            summary = summarize(execution.result),
            errorCode = failureCode(execution.result)
        )

        val updatedContext = execution.resolution.contextMutator(instance.context)
            .withRecordedStepResult(execution.stepName, recorded)

        val updatedInstance = instance.copy(
            state = execution.resolution.nextState,
            status = execution.resolution.nextStatus,
            updatedAt = Instant.now(),
            context = updatedContext,
            history = instance.history + WorkflowHistoryEntry(
                stateBefore = instance.state,
                stateAfter = execution.resolution.nextState,
                inboundEventName = inboundEvent.name,
                stepName = execution.stepName,
                outcomeType = execution.result::class.simpleName ?: "Unknown",
                summary = summarize(execution.result),
                errorCode = failureCode(execution.result),
                outboundEventNames = execution.resolution.outboundEvents.map { routedEvent: RoutedEvent ->
                    routedEvent.event.name
                }
            )
        )

        instanceStore.save(updatedInstance)

        if (execution.result is Failed) {
            eventPublisher.publish(
                WorkflowFailedEvent(
                    failure = failureDetails(
                        instance = updatedInstance,
                        stepName = execution.stepName,
                        result = execution.result
                    )
                )
            )
        }

        execution.resolution.outboundEvents.forEach { routedEvent ->
            eventPublisher.publish(
                WorkflowEventEnvelope(
                    workflowName = updatedInstance.workflowName,
                    workflowInstanceId = updatedInstance.instanceId,
                    workflowState = updatedInstance.state,
                    payload = routedEvent.event
                )
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun executeTransition(
        instance: WorkflowInstance,
        inboundEvent: Event,
        transition: WorkflowTransition<out Event, out Event>
    ): TransitionExecution {
        val typedTransition = transition as WorkflowTransition<Event, Event>

        val invocation = StepInvocation(
            instance = instance,
            event = inboundEvent,
            step = typedTransition.step
        )

        val result = stepExecutor.execute(invocation)

        val resolution = typedTransition.outcomeRouter.route(
            result = result,
            instance = instance
        )

        return TransitionExecution(
            stepName = typedTransition.step.name,
            result = result,
            resolution = resolution
        )
    }

    private fun summarize(result: StepResult<*>): String =
        when (result) {
            is Success<*> -> "Success"
            is Failed -> result.reason
            is Errored -> result.errorMessage
            is Ignored -> result.reason
            is Waiting -> result.reason
        }

    private fun failureCode(result: StepResult<*>): String? =
        when (result) {
            is Failed -> result.code
            else -> null
        }

    private fun failureDetails(
        instance: WorkflowInstance,
        stepName: String,
        result: Failed
    ): WorkflowFailureDetails =
        WorkflowFailureDetails(
            requestId = instance.context.attributes["requestId"] as? String,
            correlationId = instance.correlationId,
            instanceId = instance.instanceId,
            workflowName = instance.workflowName,
            stepName = stepName,
            errorCode = result.code,
            message = result.reason
        )

    private data class TransitionExecution(
        val stepName: String,
        val result: StepResult<*>,
        val resolution: OutcomeResolution
    )

}