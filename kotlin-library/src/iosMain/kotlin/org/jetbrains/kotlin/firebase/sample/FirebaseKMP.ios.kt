package org.jetbrains.kotlin.firebase.sample

import cocoapods.FirebaseCore.FIRApp
import org.jetbrains.kotlin.firebase.sample.services.AnalyticsService
import org.jetbrains.kotlin.firebase.sample.services.AuthService
import org.jetbrains.kotlin.firebase.sample.services.FirestoreService
import org.jetbrains.kotlin.firebase.sample.services.MessagingService

/**
 * Internal implementation of FirebaseKMP interface.
 * Delegates to per-service classes for each Firebase product.
 */
internal class FirebaseKMPImpl : FirebaseKMP {

    private val auth = AuthService()
    private val firestoreService = FirestoreService()
    private val analytics = AnalyticsService()
    private val messagingService = MessagingService()

    // --- Core ---

    override fun configure() {
        FIRApp.configure()
        analytics.enableCollection()
        firestoreService.enableOfflinePersistence()
    }

    // --- Auth ---

    override fun isUserSignedIn(): Boolean = auth.isUserSignedIn()

    override fun signIn(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit) =
        auth.signIn(email, password, completion)

    override fun signUp(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit) =
        auth.signUp(email, password, completion)

    override fun signOut(): Boolean = auth.signOut()

    override fun getCurrentUser(): FirebaseUser? = auth.getCurrentUser()

    // --- Firestore ---

    override fun saveUser(userId: String, userData: Map<String, Any>, completion: (KMPError?) -> Unit) =
        firestoreService.saveUser(userId, userData, completion)

    override fun getUser(userId: String, completion: (Map<String, Any>?, KMPError?) -> Unit) =
        firestoreService.getUser(userId, completion)

    // --- Analytics ---

    override fun logEvent(name: String, parameters: Map<String, Any>?) = analytics.logEvent(name, parameters)

    override fun setUserProperty(name: String, value: String) = analytics.setUserProperty(name, value)

    override fun setUserId(userId: String) = analytics.setUserId(userId)

    // --- Messaging ---

    override fun getMessagingToken(completion: (String?, KMPError?) -> Unit) =
        messagingService.getToken(completion)

    override fun subscribeToTopic(topic: String, completion: (KMPError?) -> Unit) =
        messagingService.subscribeToTopic(topic, completion)

    override fun unsubscribeFromTopic(topic: String, completion: (KMPError?) -> Unit) =
        messagingService.unsubscribeFromTopic(topic, completion)

    override fun deleteMessagingToken(completion: (KMPError?) -> Unit) =
        messagingService.deleteToken(completion)
}
