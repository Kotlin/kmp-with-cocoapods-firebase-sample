package org.jetbrains.kotlin.firebase.sample.samples

import cocoapods.FirebasePerformance.FIRHTTPMethod
import cocoapods.FirebasePerformance.FIRHTTPMetric
import cocoapods.FirebasePerformance.FIRPerformance
import cocoapods.FirebasePerformance.FIRTrace
import platform.Foundation.NSURL

/**
 * Sample demonstrating Firebase Performance Monitoring:
 * custom traces and HTTP metrics to measure app performance.
 */
@Suppress("UNUSED")
object PerformanceSample {

    private val performance: FIRPerformance
        get() = FIRPerformance.sharedInstance()

    /**
     * Enable or disable automatic performance data collection.
     */
    fun setDataCollectionEnabled(enabled: Boolean) {
        performance.setDataCollectionEnabled(enabled)
    }

    /**
     * Start a custom trace to measure a specific operation.
     * Returns the trace so the caller can stop it when the operation completes.
     */
    fun startTrace(name: String): FIRTrace? {
        val trace = performance.traceWithName(name)
        trace?.start()
        return trace
    }

    /**
     * Stop a custom trace.
     */
    fun stopTrace(trace: FIRTrace) {
        trace.stop()
    }

    /**
     * Run an operation wrapped in a custom trace.
     */
    fun <T> measureTrace(name: String, block: (FIRTrace?) -> T): T {
        val trace = startTrace(name)
        val result = block(trace)
        trace?.stop()
        return result
    }

    /**
     * Increment a counter metric within a trace.
     */
    fun incrementTraceCounter(trace: FIRTrace, counterName: String, by: Long = 1) {
        trace.incrementMetric(counterName, byInt = by)
    }

    /**
     * Set a custom attribute on a trace.
     */
    fun setTraceAttribute(trace: FIRTrace, name: String, value: String) {
        trace.setValue(value, forAttribute = name)
    }

    /**
     * Create and start an HTTP metric for monitoring network requests.
     */
    fun startHttpMetric(url: String, httpMethod: String): FIRHTTPMetric {
        val nsUrl = NSURL(string = url)
        val method = when (httpMethod.uppercase()) {
            "GET" -> FIRHTTPMethod.FIRHTTPMethodGET
            "POST" -> FIRHTTPMethod.FIRHTTPMethodPOST
            "PUT" -> FIRHTTPMethod.FIRHTTPMethodPUT
            "DELETE" -> FIRHTTPMethod.FIRHTTPMethodDELETE
            "PATCH" -> FIRHTTPMethod.FIRHTTPMethodPATCH
            "HEAD" -> FIRHTTPMethod.FIRHTTPMethodHEAD
            "OPTIONS" -> FIRHTTPMethod.FIRHTTPMethodOPTIONS
            else -> FIRHTTPMethod.FIRHTTPMethodGET
        }
        val metric = FIRHTTPMetric(uRL = nsUrl, HTTPMethod = method)
        metric.start()
        return metric
    }

    /**
     * Stop an HTTP metric with response info.
     */
    fun stopHttpMetric(metric: FIRHTTPMetric, responseCode: Int) {
        metric.setResponseCode(responseCode.toLong())
        metric.stop()
    }
}
