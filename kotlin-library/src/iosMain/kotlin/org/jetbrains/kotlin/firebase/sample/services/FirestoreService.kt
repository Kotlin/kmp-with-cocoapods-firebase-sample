package org.jetbrains.kotlin.firebase.sample.services

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRDocumentReference
import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRFirestore
import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRFirestoreSettings
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError

@Suppress("UNCHECKED_CAST")
internal class FirestoreService {

    private val firestore: FIRFirestore
        get() = FIRFirestore.firestore()

    fun enableOfflinePersistence() {
        val settings = FIRFirestoreSettings()
        settings.setPersistenceEnabled(true)
        firestore.setSettings(settings)
    }

    fun saveUser(userId: String, userData: Map<String, Any>, completion: (KMPError?) -> Unit) {
        val userDocument: FIRDocumentReference = firestore
            .collectionWithPath("users")
            .documentWithPath(userId)

        val mutableUserData = userData.toMutableMap()
        userDocument.setData(mutableUserData as Map<Any?, *>, true) { error ->
            completion(error?.toKMPError())
        }
    }

    fun getUser(userId: String, completion: (Map<String, Any>?, KMPError?) -> Unit) {
        val userDocument: FIRDocumentReference = firestore
            .collectionWithPath("users")
            .documentWithPath(userId)

        userDocument.getDocumentWithCompletion { snapshot, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else if (snapshot?.exists() == true) {
                val data = snapshot.data() as? Map<String, Any>
                completion(data, null)
            } else {
                completion(null, null)
            }
        }
    }
}
