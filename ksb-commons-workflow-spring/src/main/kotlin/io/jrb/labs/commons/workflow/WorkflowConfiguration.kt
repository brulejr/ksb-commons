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

package io.jrb.labs.commons.workflow

import io.jrb.labs.commons.eventbus.EventBus
import io.jrb.labs.commons.workflow.api.WorkflowDefinition
import io.jrb.labs.commons.workflow.api.WorkflowRegistry
import io.jrb.labs.commons.workflow.engine.DefaultWorkflowTrafficCop
import io.jrb.labs.commons.workflow.engine.StepExecutor
import io.jrb.labs.commons.workflow.engine.TransitionMatcher
import io.jrb.labs.commons.workflow.engine.WorkflowMiddleware
import io.jrb.labs.commons.workflow.engine.WorkflowTrafficCop
import io.jrb.labs.commons.workflow.spi.WorkflowInstanceStore
import io.jrb.labs.commons.workflow.support.DefaultWorkflowRegistry
import io.jrb.labs.commons.workflow.support.InMemoryWorkflowInstanceStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WorkflowConfiguration {

    @Bean
    fun trafficCop(
        workflowRegistry: WorkflowRegistry,
        instanceStore: WorkflowInstanceStore,
        transitionMatcher: TransitionMatcher,
        stepExecutor: StepExecutor,
        eventBus: EventBus
    ): WorkflowTrafficCop = DefaultWorkflowTrafficCop(
        workflowRegistry, instanceStore, transitionMatcher, stepExecutor, eventBus
    )

    @Bean
    fun workflowRegistry(workflowDefinitions: List<WorkflowDefinition>): WorkflowRegistry =
        DefaultWorkflowRegistry(workflowDefinitions)

    @Bean
    fun instanceStore(): WorkflowInstanceStore = InMemoryWorkflowInstanceStore()

    @Bean
    fun transitionMatcher(): TransitionMatcher = TransitionMatcher()

    @Bean
    fun stepExecutor(middlewares: List<WorkflowMiddleware>): StepExecutor = StepExecutor(middlewares)

}