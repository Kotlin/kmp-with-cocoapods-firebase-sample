package org.jetbrains.kotlin.firebase.sample.samples

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRInstallations
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError

/**
 * Sample demonstrating Firebase Installations:
 * managing unique installation identifiers and auth tokens.
 */
@Suppress("UNUSED")
object InstallationsSample {

    private val installations: FIRInstallations
        get() = FIRInstallations.installations()

    /**
     * Get the Firebase Installation ID (FID).
     * The FID uniquely identifies this app installation.
     */
    fun getInstallationId(completion: (String?, KMPError?) -> Unit) {
        installations.installationIDWithCompletion { id, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(id, null)
            }
        }
    }

    /**
     * Get the installation auth token.
     * This token can be used to authenticate requests to your backend.
     */
    fun getAuthToken(forceRefresh: Boolean = false, completion: (String?, KMPError?) -> Unit) {
        installations.authTokenForcingRefresh(forceRefresh) { tokenResult, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(tokenResult?.authToken(), null)
            }
        }
    }

    /**
     * Delete the current installation and its data.
     * A new FID will be generated on next access.
     */
    fun deleteInstallation(completion: (KMPError?) -> Unit) {
        installations.deleteWithCompletion { error ->
            completion(error?.toKMPError())
        }
    }
}
