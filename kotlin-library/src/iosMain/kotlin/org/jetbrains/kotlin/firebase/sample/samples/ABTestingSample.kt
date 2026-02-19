package org.jetbrains.kotlin.firebase.sample.samples

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfig
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSettings
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError

/**
 * Sample demonstrating Firebase A/B Testing.
 *
 * A/B Testing works through Remote Config: experiments are defined in the
 * Firebase console and delivered as Remote Config parameters. The ABTesting
 * pod provides the underlying experiment tracking infrastructure.
 *
 * This sample shows how to use Remote Config to read experiment values
 * and act on them, which is the client-side pattern for A/B Testing.
 */
@Suppress("UNUSED")
object ABTestingSample {

    private val remoteConfig: FIRRemoteConfig
        get() = FIRRemoteConfig.remoteConfig()

    /**
     * Set up Remote Config with a short fetch interval for testing experiments.
     */
    fun configureForExperiments() {
        val settings = FIRRemoteConfigSettings()
        settings.setMinimumFetchInterval(0.0) // No throttle for testing
        remoteConfig.setConfigSettings(settings)
    }

    /**
     * Fetch and activate experiment parameters.
     */
    fun fetchExperimentConfig(completion: (KMPError?) -> Unit) {
        remoteConfig.fetchAndActivateWithCompletionHandler { _, error ->
            if (error != null) {
                completion(error.toKMPError())
            } else {
                completion(null)
            }
        }
    }

    /**
     * Get the experiment variant for a given experiment key.
     * Returns the variant string (e.g. "control", "variant_a", "variant_b").
     */
    fun getExperimentVariant(experimentKey: String): String {
        return remoteConfig.configValueForKey(experimentKey).stringValue().ifEmpty { "control" }
    }

    /**
     * Check if a feature flag from an A/B test is enabled.
     */
    fun isFeatureEnabled(featureKey: String): Boolean {
        return remoteConfig.configValueForKey(featureKey).boolValue()
    }

    /**
     * Example: apply experiment-driven UI based on A/B test variant.
     */
    fun applyExperiment(
        experimentKey: String,
        onControl: () -> Unit,
        onVariantA: () -> Unit,
        onVariantB: () -> Unit
    ) {
        when (getExperimentVariant(experimentKey)) {
            "control" -> onControl()
            "variant_a" -> onVariantA()
            "variant_b" -> onVariantB()
            else -> onControl()
        }
    }
}
