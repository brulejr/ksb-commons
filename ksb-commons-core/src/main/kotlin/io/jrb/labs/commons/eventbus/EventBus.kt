/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Jon Brule <brulejr@gmail.com>
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
package io.jrb.labs.commons.eventbus

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class EventBus<E : Event>(
    eventBusCapacity: Int = 100,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val events = MutableSharedFlow<E>(extraBufferCapacity = eventBusCapacity)

    /**
     * Publish an event to all subscribers.
     */
    suspend fun publish(event: E) {
        events.emit(event)
    }

    /**
     * Publish an event to all subscribers.
     */
    fun send(event: E) = runBlocking { publish(event) }

    /**
     * Catch-all subscription for consumers like the workflow traffic cop.
     */
    fun subscribeAll(handler: suspend (E) -> Unit): Subscription {
        val job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            events.collect { handler(it) }
        }
        return Subscription(job)
    }

    /**
     * Subscribe to a specific event type.
     */
    fun <T : E> subscribe(clazz: Class<T>, handler: suspend (T) -> Unit): Subscription {
        val job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            events
                .filter { clazz.isInstance(it) }
                .map { clazz.cast(it)!! }
                .collect { handler(it) }
        }
        return Subscription(job)
    }

    /**
     * Subscribe to a specific event type.
     */
    inline fun <reified T : E> subscribe(noinline handler: suspend (T) -> Unit): Subscription =
        subscribe(T::class.java, handler)

    /**
     * Close the event bus and cancel all subscriptions.
     */
    fun close() {
        scope.cancel()
    }

    /**
     * Represents a subscription to an event bus.
     */
    class Subscription(private val job: Job) {
        fun cancel() = job.cancel()
    }

}