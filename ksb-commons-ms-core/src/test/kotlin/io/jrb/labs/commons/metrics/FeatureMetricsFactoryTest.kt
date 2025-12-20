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
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FeatureMetricsFactoryTest {

    private val application = "myapp"
    private val featureId1 = "feat1"
    private val featureId2 = "feat2"

    private fun mockDescriptor(app: String, featureId: String): FeatureDescriptor {
        val descriptor = mockk<FeatureDescriptor>()
        every { descriptor.application } returns app
        every { descriptor.featureId } returns featureId
        return descriptor
    }

    @Test
    fun `forFeature creates FeatureMetrics that register metrics on provided registry`() {
        val registry = SimpleMeterRegistry()
        val descriptor = mockDescriptor(application, featureId1)
        val factory = FeatureMetricsFactory(registry)

        val fm = factory.forFeature(descriptor)
        fm.eventCounter("ingest").increment(2.0)

        val found = registry.find("${application}_feature_events_total")
            .tags("feature", featureId1, "stage", "ingest")
            .counter()

        assertThat(found).isNotNull
        assertThat(found!!.count()).isEqualTo(2.0)
    }

    @Test
    fun `forFeature returns distinct FeatureMetrics instances on each call`() {
        val registry = SimpleMeterRegistry()
        val descriptor = mockDescriptor(application, featureId1)
        val factory = FeatureMetricsFactory(registry)

        val fm1 = factory.forFeature(descriptor)
        val fm2 = factory.forFeature(descriptor)

        assertThat(fm1).isNotSameAs(fm2)
    }

    @Test
    fun `different descriptors produce metrics tagged by their feature ids`() {
        val registry = SimpleMeterRegistry()
        val descriptor1 = mockDescriptor(application, featureId1)
        val descriptor2 = mockDescriptor(application, featureId2)
        val factory = FeatureMetricsFactory(registry)

        val fm1 = factory.forFeature(descriptor1)
        val fm2 = factory.forFeature(descriptor2)

        fm1.errorCounter("processing").increment(1.0)
        fm2.errorCounter("processing").increment(3.0)

        val found1 = registry.find("${application}_feature_errors_total")
            .tags("feature", featureId1, "stage", "processing")
            .counter()
        val found2 = registry.find("${application}_feature_errors_total")
            .tags("feature", featureId2, "stage", "processing")
            .counter()

        assertThat(found1).isNotNull
        assertThat(found1!!.count()).isEqualTo(1.0)

        assertThat(found2).isNotNull
        assertThat(found2!!.count()).isEqualTo(3.0)
    }
}
