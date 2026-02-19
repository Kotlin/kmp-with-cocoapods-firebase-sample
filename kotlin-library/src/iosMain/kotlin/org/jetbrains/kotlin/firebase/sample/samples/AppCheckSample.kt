package org.jetbrains.kotlin.firebase.sample.samples

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRAppCheck
import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRAppCheckDebugProviderFactory
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError

/**
 * Sample demonstrating Firebase App Check:
 * verifying that requests come from your authentic app.
 */
@Suppress("UNUSED")
object AppCheckSample {

    /**
     * Configure App Check with the debug provider (for development/testing).
     * In production, use DeviceCheck or App Attest provider instead.
     */
    fun configureDebugProvider() {
        val providerFactory = FIRAppCheckDebugProviderFactory()
        FIRAppCheck.setAppCheckProviderFactory(providerFactory)
    }

    /**
     * Get a limited-use App Check token.
     */
    fun getToken(completion: (String?, KMPError?) -> Unit) {
        FIRAppCheck.appCheck().tokenForcingRefresh(false) { token, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(token?.token(), null)
            }
        }
    }

    /**
     * Force refresh the App Check token.
     */
    fun getTokenForcingRefresh(completion: (String?, KMPError?) -> Unit) {
        FIRAppCheck.appCheck().tokenForcingRefresh(true) { token, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(token?.token(), null)
            }
        }
    }

    /**
     * Get a limited-use token for use with custom backends.
     */
    fun getLimitedUseToken(completion: (String?, KMPError?) -> Unit) {
        FIRAppCheck.appCheck().limitedUseTokenWithCompletion { token, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(token?.token(), null)
            }
        }
    }
}
