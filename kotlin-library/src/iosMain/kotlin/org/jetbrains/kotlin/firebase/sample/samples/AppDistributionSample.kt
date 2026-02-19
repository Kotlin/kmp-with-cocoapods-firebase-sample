package org.jetbrains.kotlin.firebase.sample.samples

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRAppDistribution
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError

/**
 * Sample demonstrating Firebase App Distribution:
 * checking for and prompting updates of pre-release builds.
 */
@Suppress("UNUSED")
object AppDistributionSample {

    private val appDistribution: FIRAppDistribution
        get() = FIRAppDistribution.appDistribution()

    /**
     * Check if the current user is a signed-in tester.
     */
    fun isTesterSignedIn(): Boolean {
        return appDistribution.isTesterSignedIn()
    }

    /**
     * Sign in the tester to enable feedback features.
     */
    fun signInTester(completion: (KMPError?) -> Unit) {
        appDistribution.signInTesterWithCompletion { error ->
            completion(error?.toKMPError())
        }
    }

    /**
     * Check if there's a new release available.
     */
    fun checkForUpdate(completion: (Boolean, KMPError?) -> Unit) {
        appDistribution.checkForUpdateWithCompletion { release, error ->
            if (error != null) {
                completion(false, error.toKMPError())
            } else {
                completion(release != null, null)
            }
        }
    }

    /**
     * Sign out the tester.
     */
    fun signOutTester() {
        appDistribution.signOutTester()
    }
}
