package org.jetbrains.kotlin.firebase.sample.services

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRMessaging
import org.jetbrains.kotlin.firebase.sample.KMPError
import org.jetbrains.kotlin.firebase.sample.toKMPError

internal class MessagingService {

    private val messaging: FIRMessaging
        get() = FIRMessaging.messaging()

    fun getToken(completion: (String?, KMPError?) -> Unit) {
        messaging.tokenWithCompletion { token, error ->
            if (error != null) {
                completion(null, error.toKMPError())
            } else {
                completion(token, null)
            }
        }
    }

    fun subscribeToTopic(topic: String, completion: (KMPError?) -> Unit) {
        messaging.subscribeToTopic(topic) { error ->
            completion(error?.toKMPError())
        }
    }

    fun unsubscribeFromTopic(topic: String, completion: (KMPError?) -> Unit) {
        messaging.unsubscribeFromTopic(topic) { error ->
            completion(error?.toKMPError())
        }
    }

    fun deleteToken(completion: (KMPError?) -> Unit) {
        messaging.deleteTokenWithCompletion { error ->
            completion(error?.toKMPError())
        }
    }
}
