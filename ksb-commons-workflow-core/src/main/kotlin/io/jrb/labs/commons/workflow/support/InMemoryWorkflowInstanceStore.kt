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

package io.jrb.labs.commons.workflow.support

import io.jrb.labs.commons.workflow.api.WorkflowInstance
import io.jrb.labs.commons.workflow.spi.WorkflowInstanceStore
import java.util.concurrent.ConcurrentHashMap

class InMemoryWorkflowInstanceStore : WorkflowInstanceStore {

    private val instances = ConcurrentHashMap<String, WorkflowInstance>()

    override suspend fun save(instance: WorkflowInstance): WorkflowInstance {
        instances[instance.instanceId] = instance
        return instance
    }

    override suspend fun findByInstanceId(instanceId: String): WorkflowInstance? =
        instances[instanceId]

    override suspend fun findByCorrelationId(correlationId: String): List<WorkflowInstance> =
        instances.values.filter { it.correlationId == correlationId }

    override suspend fun findAll(): List<WorkflowInstance> =
        instances.values
            .sortedByDescending { it.updatedAt }

}
