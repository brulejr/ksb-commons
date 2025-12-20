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

package io.jrb.labs.commons.metrics

import io.jrb.labs.commons.feature.FeatureDescriptor
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.TimeUnit

class FeatureMetricsTest {

    private val application = "myapp"
    private val featureId = "feat1"

    private fun createFeatureMetrics(): Pair<SimpleMeterRegistry, FeatureMetrics> {
        val registry = SimpleMeterRegistry()
        val descriptor = mockk<FeatureDescriptor>()
        every { descriptor.application } returns application
        every { descriptor.featureId } returns featureId
        val fm = FeatureMetrics(registry, descriptor)
        return registry to fm
    }

    @Test
    fun `errorCounter registers counter with tags and increments`() {
        val (registry, fm) = createFeatureMetrics()
        val counter = fm.errorCounter("processing")
        counter.increment(2.0)

        val found = registry.find("${application}_feature_errors_total")
            .tags("feature", featureId, "stage", "processing")
            .counter()

        assertThat(found).isNotNull
        assertThat(found!!.count()).isEqualTo(2.0)
    }

    @Test
    fun `eventCounter registers counter with tags and increments`() {
        val (registry, fm) = createFeatureMetrics()
        val counter = fm.eventCounter("ingest")
        counter.increment(3.0)

        val found = registry.find("${application}_feature_events_total")
            .tags("feature", featureId, "stage", "ingest")
            .counter()

        assertThat(found).isNotNull
        assertThat(found!!.count()).isEqualTo(3.0)
    }

    @Test
    fun `featureStateGauge reports 1 for up and 0 for down and handles stage`() {
        val (registry, fm) = createFeatureMetrics()

        // "up" gauge without explicit stage (stage defaults to empty string)
        val upGauge = fm.featureStateGauge({ true })
        val foundUp = registry.find("${application}_feature_state")
            .tags("feature", featureId, "stage", "")
            .gauge()
        assertThat(foundUp).isNotNull
        assertThat(foundUp!!.value()).isEqualTo(1.0)

        // "down" gauge with explicit stage
        val downGauge = fm.featureStateGauge({ false }, "discovery")
        val foundDown = registry.find("${application}_feature_state")
            .tags("feature", featureId, "stage", "discovery")
            .gauge()
        assertThat(foundDown).isNotNull
        assertThat(foundDown!!.value()).isEqualTo(0.0)
    }

    @Test
    fun `processingTimer records a single timing and returns block result`() = runBlocking {
        val (registry, fm) = createFeatureMetrics()

        val result = fm.processingTimer("proc") {
            // simulate work
            Thread.sleep(10)
            "ok"
        }

        assertThat(result).isEqualTo("ok")

        val timer = registry.find("${application}_feature_processing_seconds")
            .tags("feature", featureId, "stage", "proc")
            .timer()

        assertThat(timer).isNotNull
        assertThat(timer!!.count()).isEqualTo(1)
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0.0)
    }

}
