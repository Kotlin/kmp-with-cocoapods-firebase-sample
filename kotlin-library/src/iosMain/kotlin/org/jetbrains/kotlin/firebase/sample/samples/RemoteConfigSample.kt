package org.jetbrains.kotlin.firebase.sample.samples

import cocoapods.FirebaseRemoteConfig.FIRRemoteConfig
import cocoapods.FirebaseRemoteConfig.FIRRemoteConfigSettings
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError

/**
 * Sample demonstrating Firebase Remote Config:
 * fetching and activating remote parameters to customize app behavior.
 */
@Suppress("UNUSED")
object RemoteConfigSample {

    private val remoteConfig: FIRRemoteConfig
        get() = FIRRemoteConfig.remoteConfig()

    /**
     * Set up Remote Config with developer mode settings.
     */
    fun configure(minimumFetchIntervalSeconds: Double = 3600.0) {
        val settings = FIRRemoteConfigSettings()
        settings.setMinimumFetchInterval(minimumFetchIntervalSeconds)
        remoteConfig.setConfigSettings(settings)
    }

    /**
     * Set default values for Remote Config parameters.
     */
    fun setDefaults(defaults: Map<Any?, *>) {
        remoteConfig.setDefaults(defaults)
    }

    /**
     * Fetch remote config values from the server.
     */
    fun fetch(completion: (KMPError?) -> Unit) {
        remoteConfig.fetchWithCompletionHandler { status, error ->
            if (error != null) {
                completion(error.toKMPError())
            } else {
                completion(null)
            }
        }
    }

    /**
     * Fetch and activate in one step.
     */
    fun fetchAndActivate(completion: (Boolean, KMPError?) -> Unit) {
        remoteConfig.fetchAndActivateWithCompletionHandler { status, error ->
            if (error != null) {
                completion(false, error.toKMPError())
            } else {
                completion(true, null)
            }
        }
    }

    /**
     * Activate fetched config values.
     */
    fun activate(completion: (Boolean, KMPError?) -> Unit) {
        remoteConfig.activateWithCompletion { changed, error ->
            if (error != null) {
                completion(false, error.toKMPError())
            } else {
                completion(changed, null)
            }
        }
    }

    /**
     * Get a string value for a key.
     */
    fun getString(key: String): String {
        return remoteConfig.configValueForKey(key).stringValue()
    }

    /**
     * Get a boolean value for a key.
     */
    fun getBoolean(key: String): Boolean {
        return remoteConfig.configValueForKey(key).boolValue()
    }

    /**
     * Get a number value for a key.
     */
    fun getNumber(key: String): Double {
        return remoteConfig.configValueForKey(key).numberValue().doubleValue
    }
}
