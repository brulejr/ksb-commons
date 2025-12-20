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
import io.micrometer.core.instrument.MeterRegistry

/**
 * Factory class for creating instances of `FeatureMetrics` specific to a given feature descriptor.
 *
 * This class provides a streamlined way to initialize feature-specific metrics
 * by utilizing a shared `MeterRegistry`. It allows monitoring and observing various
 * metrics such as error counts, event counts, feature state, and processing latency
 * for individual features, as described by their respective `FeatureDescriptor`.
 *
 * @constructor Initializes the factory with a `MeterRegistry` for registering metrics.
 * @param registry The `MeterRegistry` used for metric registration and monitoring.
 */
class FeatureMetricsFactory(
    private val registry: MeterRegistry
) {

    /**
     * Creates a `FeatureMetrics` instance for monitoring metrics specific to the given `FeatureDescriptor`.
     *
     * @param featureDescriptor The descriptor representing the application's feature, including its details such as ID, name, and description.
     * @return A `FeatureMetrics` instance configured for the specified `FeatureDescriptor`.
     */
    fun forFeature(featureDescriptor: FeatureDescriptor): FeatureMetrics =
        FeatureMetrics(registry, featureDescriptor)

}