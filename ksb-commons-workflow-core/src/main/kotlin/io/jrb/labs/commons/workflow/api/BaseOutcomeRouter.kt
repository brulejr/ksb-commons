package io.jrb.labs.commons.workflow.api

import io.jrb.labs.commons.eventbus.Event

abstract class BaseOutcomeRouter<O : Event> : OutcomeRouter<O> {

    override fun route(
        result: StepResult<O>,
        instance: WorkflowInstance
    ): OutcomeResolution {
        return when (result) {
            is StepResult.Success -> successFn(result)
            is StepResult.Failed -> failureFn(result, instance)
            is StepResult.Errored -> erroredFn(result, instance)
            is StepResult.Ignored -> ignoredFn(result, instance)
            is StepResult.Waiting -> waitingFn(result, instance)
        }
    }

    abstract fun successFn(result: StepResult.Success<O>): OutcomeResolution

    protected open fun failureFn(result: StepResult.Failed, instance: WorkflowInstance): OutcomeResolution {
        return OutcomeResolution(
            nextState = "FAILED",
            nextStatus = WorkflowStatus.FAILED
        )
    }

    protected open fun erroredFn(result: StepResult.Errored, instance: WorkflowInstance): OutcomeResolution {
        return OutcomeResolution(
            nextState = "ERRORED",
            nextStatus = WorkflowStatus.ERRORED
        )
    }

    protected open fun ignoredFn(result: StepResult.Ignored, instance: WorkflowInstance): OutcomeResolution {
        return OutcomeResolution(
            nextState = instance.state,
            nextStatus = instance.status
        )
    }

    protected open fun waitingFn(result: StepResult.Waiting, instance: WorkflowInstance): OutcomeResolution {
        return OutcomeResolution(
            nextState = instance.state,
            nextStatus = WorkflowStatus.WAITING
        )
    }

}