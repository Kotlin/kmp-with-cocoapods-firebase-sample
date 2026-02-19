package org.jetbrains.kotlin.firebase.sample

/**
 * Interface representing a mockable implementation of the `FirebaseKMP` interface.
 * Designed to facilitate testing by providing a mechanism to reset mock data or state.
 *
 * Extends the `FirebaseKMP` interface, which provides core Firebase-related operations
 * like authentication, Firestore interactions, and analytics event logging.
 */
interface MockFirebaseKMP: FirebaseKMP {
    var simulateError: KMPError?
    var signedInUserOverride: FirebaseUser?
    var nextUserToReturnOnAuth: FirebaseUser?
    var userDatabase: MutableMap<String, Map<String, Any>>
    var shouldSignOutSucceed: Boolean
    var autoSignInOnAuthSuccess: Boolean

    fun reset()
}

/**
 * Interface for Firebase KMP operations
 */
interface FirebaseKMP {
    /**
     * Configure and initialize Firebase
     */
    fun configure()

    /**
     * Check if a user is currently signed in
     * @return Boolean indicating if user is signed in
     */
    fun isUserSignedIn(): Boolean

    /**
     * Sign in with email and password
     * @param email User email
     * @param password User password
     * @param completion Callback with result or error
     */
    fun signIn(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit)

    /**
     * Create new user with email and password
     * @param email User email
     * @param password User password
     * @param completion Callback with result or error
     */
    fun signUp(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit)

    /**
     * Sign out current user
     * @return Boolean indicating success
     */
    fun signOut(): Boolean

    /**
     * Save user data to Firestore
     * @param userId User identifier
     * @param userData Map of user data
     * @param completion Callback with error if any
     */
    fun saveUser(userId: String, userData: Map<String, Any>, completion: (KMPError?) -> Unit)

    /**
     * Get user data from Firestore
     * @param userId User identifier
     * @param completion Callback with user data or error
     */
    fun getUser(userId: String, completion: (Map<String, Any>?, KMPError?) -> Unit)

    /**
     * Log analytics event
     * @param name Event name
     * @param parameters Optional event parameters
     */
    fun logEvent(name: String, parameters: Map<String, Any>? = null)

    /**
     * Set user property for analytics
     * @param name Property name
     * @param value Property value
     */
    fun setUserProperty(name: String, value: String)

    /**
     * Set user ID for analytics
     * @param userId User identifier
     */
    fun setUserId(userId: String)

    /**
     * Get current user information
     * @return FirebaseUser object or null if not signed in
     */
    fun getCurrentUser(): FirebaseUser?

    // --- Firebase Messaging ---

    /**
     * Get the current FCM registration token
     * @param completion Callback with token string or error
     */
    fun getMessagingToken(completion: (String?, KMPError?) -> Unit)

    /**
     * Subscribe to a messaging topic
     * @param topic Topic name to subscribe to
     * @param completion Callback with error if any
     */
    fun subscribeToTopic(topic: String, completion: (KMPError?) -> Unit)

    /**
     * Unsubscribe from a messaging topic
     * @param topic Topic name to unsubscribe from
     * @param completion Callback with error if any
     */
    fun unsubscribeFromTopic(topic: String, completion: (KMPError?) -> Unit)

    /**
     * Delete the current FCM token, effectively unregistering the device
     * @param completion Callback with error if any
     */
    fun deleteMessagingToken(completion: (KMPError?) -> Unit)
}