/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2026 Jon Brule <brulejr@gmail.com>
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
import io.jrb.labs.commons.workflow.api.RecordedStepResult
import io.jrb.labs.commons.workflow.api.StepResult
import io.jrb.labs.commons.workflow.api.StepResult.*
import io.jrb.labs.commons.workflow.api.WorkflowDefinition
import io.jrb.labs.commons.workflow.api.WorkflowHistoryEntry
import io.jrb.labs.commons.workflow.api.WorkflowInstance
import io.jrb.labs.commons.workflow.api.WorkflowRegistry
import io.jrb.labs.commons.workflow.api.WorkflowStatus
import io.jrb.labs.commons.workflow.api.WorkflowTransition
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
        val byCorrelation = event.correlationId
            ?.let { instanceStore.findByCorrelationId(it) }
            .orEmpty()

        if (byCorrelation.isNotEmpty()) {
            byCorrelation.forEach { advanceExistingWorkflow(it, event) }
            return
        }

        val primedDefinitions = workflowRegistry.findByPrimingEvent(event)
        primedDefinitions.forEach { definition ->
            startWorkflow(definition, event)
        }
    }

    private suspend fun startWorkflow(
        definition: WorkflowDefinition,
        event: Event
    ) {
        val correlationId = definition.correlationIdOf(event)

        val instance = WorkflowInstance(
            workflowName = definition.name,
            correlationId = correlationId,
            state = definition.initialState,
            status = WorkflowStatus.RUNNING
        )

        instanceStore.save(instance)
        advanceExistingWorkflow(instance, event)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun advanceExistingWorkflow(
        instance: WorkflowInstance,
        event: Event
    ) {
        val definition = workflowRegistry.definitions()
            .firstOrNull { it.name == instance.workflowName }
            ?: return

        val transition = transitionMatcher.findMatchingTransition(
            definition = definition,
            currentState = instance.state,
            event = event
        ) ?: return

        val typedTransition = transition as WorkflowTransition<Event, Event>
        val typedEvent = event as Event

        val invocation = StepInvocation(
            instance = instance,
            event = typedEvent,
            step = typedTransition.step
        )

        val result = stepExecutor.execute(invocation)
        val resolution = typedTransition.outcomeRouter.route(result, instance)

        val recorded = RecordedStepResult(
            stepName = typedTransition.step.name,
            outcomeType = result::class.simpleName ?: "Unknown",
            summary = summarize(result)
        )

        val updatedContext = resolution.contextMutator(instance.context)
            .withRecordedStepResult(typedTransition.step.name, recorded)

        val updatedInstance = instance.copy(
            state = resolution.nextState,
            status = resolution.nextStatus,
            updatedAt = Instant.now(),
            context = updatedContext,
            history = instance.history + WorkflowHistoryEntry(
                stateBefore = instance.state,
                stateAfter = resolution.nextState,
                inboundEventName = event.name,
                stepName = typedTransition.step.name,
                outcomeType = result::class.simpleName ?: "Unknown",
                summary = summarize(result),
                outboundEventNames = resolution.outboundEvents.map { it.event.name }
            )
        )

        instanceStore.save(updatedInstance)

        resolution.outboundEvents.forEach { routed ->
            eventPublisher.publish(routed.event)
        }
    }

    private fun summarize(result: StepResult<*>): String =
        when (result) {
            is Success<*> -> "Success"
            is Failed -> result.reason
            is Errored -> result.errorMessage
            is Ignored -> result.reason
            is Waiting -> result.reason
        }

}
