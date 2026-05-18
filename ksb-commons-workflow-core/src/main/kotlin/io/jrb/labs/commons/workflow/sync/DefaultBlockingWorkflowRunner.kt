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

package io.jrb.labs.commons.workflow.sync

import io.jrb.labs.commons.eventbus.Event
import io.jrb.labs.commons.eventbus.EventBus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class DefaultBlockingWorkflowRunner(
    private val eventBus: EventBus<Event>
) : BlockingWorkflowRunner {

    private val waiters = ConcurrentHashMap<String, CompletableDeferred<Event>>()

    override suspend fun <T : Event> runUntilCompleted(
        correlationId: String,
        startEvent: Event,
        timeout: Duration
    ): T {
        val deferred = CompletableDeferred<Event>()
        val previous = waiters.putIfAbsent(correlationId, deferred)

        require(previous == null) {
            "A blocking workflow run is already waiting on correlationId=$correlationId"
        }

        return try {
            eventBus.publish(startEvent)

            @Suppress("UNCHECKED_CAST")
            withTimeout(timeout.toMillis()) {
                deferred.await() as T
            }
        } finally {
            waiters.remove(correlationId, deferred)
        }
    }

    fun complete(
        correlationId: String,
        terminalEvent: Event
    ) {
        waiters.remove(correlationId)?.complete(terminalEvent)
    }

    fun fail(
        correlationId: String,
        throwable: Throwable
    ) {
        waiters.remove(correlationId)?.completeExceptionally(throwable)
    }
}