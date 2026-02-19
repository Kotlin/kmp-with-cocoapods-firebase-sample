package org.jetbrains.kotlin.firebase.sample.services

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRAuth
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.jetbrains.kotlin.firebase.sample.FirebaseUser
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError
import org.jetbrains.kotlin.firebase.sample.toKMPUser
import platform.Foundation.NSError

internal class AuthService {

    private val firebaseAuth: FIRAuth
        get() = FIRAuth.auth()

    fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser() != null
    }

    fun signIn(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit) {
        firebaseAuth.signInWithEmail(email, password = password) { authResult, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(authResult?.user()?.toKMPUser(), null)
            }
        }
    }

    fun signUp(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit) {
        firebaseAuth.createUserWithEmail(email, password) { authResult, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(authResult?.user()?.toKMPUser(), null)
            }
        }
    }

    @OptIn(BetaInteropApi::class)
    fun signOut(): Boolean {
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

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser()?.toKMPUser()
    }
}
