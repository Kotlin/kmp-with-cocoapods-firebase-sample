package org.jetbrains.kotlin.firebase.sample

import cocoapods.FirebaseAnalytics.FIRAnalytics
import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRUser
import cocoapods.FirebaseCore.FIRApp
import cocoapods.FirebaseFirestoreInternal.FIRDocumentReference
import cocoapods.FirebaseFirestoreInternal.FIRFirestore
import cocoapods.FirebaseFirestoreInternal.FIRFirestoreSettings
import platform.Foundation.NSError
import kotlinx.cinterop.*

/**
 * Internal implementation of FirebaseKMP interface
 */
@Suppress("UNCHECKED_CAST")
internal class FirebaseKMPImpl : FirebaseKMP {

    // Convenience getter for the Firebase Auth instance
    private val firebaseAuth: FIRAuth
        get() = FIRAuth.auth()

    // Convenience getter for the Firestore instance
    private val firestore: FIRFirestore
        get() = FIRFirestore.firestore()

    override fun configure() {
        // Initialize Firebase
        FIRApp.configure()

        // Optional: Set Firebase Analytics collection enabled
        FIRAnalytics.setAnalyticsCollectionEnabled(true)

        // Optional: Enable Firestore offline persistence
        val settings = FIRFirestoreSettings()
        settings.setPersistenceEnabled(true)
        FIRFirestore.firestore().setSettings(settings)
    }

    override fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser() != null
    }

    override fun signIn(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit) {
        firebaseAuth.signInWithEmail(email, password = password) { authResult, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(authResult?.user()?.toKMPUser(), null)
            }
        }
    }

    override fun signUp(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit) {
        firebaseAuth.createUserWithEmail(email, password) { authResult, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(authResult?.user()?.toKMPUser(), null)
            }
        }
    }

    @OptIn(BetaInteropApi::class)
    override fun signOut(): Boolean {
        return memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>().ptr
            val status = FIRAuth.auth().signOut(errorPtr)

            if (!status) {
                val error = errorPtr.pointed.value
                println("Error signing out: ${error?.localizedDescription}")
            }

            status
        }
    }

    override fun saveUser(userId: String, userData: Map<String, Any>, completion: (KMPError?) -> Unit) {
        // Convert KMP Map<String, Any> to a type suitable for Firestore if needed.
        // Firestore typically handles standard Swift/Obj-C dictionary types well.
        val userDocument: FIRDocumentReference = firestore
            .collectionWithPath("users")
            .documentWithPath(userId)

        // Use a mutable map to ensure compatibility with Firestore's setData method,
        // which expects a dictionary that can be bridged to NSDictionary.
        val mutableUserData = userData.toMutableMap()

        userDocument.setData(mutableUserData as Map<Any?, *>, true) { error ->
            completion(error?.toKMPError())
        }
    }

    override fun getUser(userId: String, completion: (Map<String, Any>?, KMPError?) -> Unit) {
        val userDocument: FIRDocumentReference = firestore
            .collectionWithPath("users")
            .documentWithPath(userId)

        // Fetch the document from Firestore
        userDocument.getDocumentWithCompletion { snapshot, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else if (snapshot?.exists() == true) {
                // Convert Firestore data (NSDictionary) to Swift's [String: Any]
                // then to KMP's Map<String, Any>
                val data = snapshot.data() as? Map<String, Any>
                completion(data, null)
            } else {
                // Document does not exist
                completion(null, null) // Or a custom "not found" error
            }
        }
    }

    override fun logEvent(name: String, parameters: Map<String, Any>?) {
        // Convert KMP Map<String, Any>? to Swift's [String: Any]?
        // FIRAnalytics.logEventWithName expects [String: Any]?
        val swiftParameters = parameters?.let { it as? Map<Any?, *> }
        FIRAnalytics.logEventWithName(name, swiftParameters)
    }

    override fun setUserProperty(name: String, value: String) {
        FIRAnalytics.setUserPropertyString(value, name)
    }

    override fun setUserId(userId: String) {
        FIRAnalytics.setUserID(userId)
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser()?.toKMPUser()
    }
}

// --- Helper extension function to convert native FIRUser to KMP FirebaseUser ---
private fun FIRUser.toKMPUser(): FirebaseUser {
    return FirebaseUser(
        uid = this.uid(),
        email = this.email() ?: "", // KMP FirebaseUser expects non-null email
        displayName = this.displayName(),
        photoURL = this.photoURL()?.absoluteString
    )
}

private fun NSError.toKMPError(): KMPError {
    return KMPError(this)
}