package org.jetbrains.kotlin.firebase.sample

@Suppress("UNUSED")
internal class MockFirebaseKMPImpl : MockFirebaseKMP {

    // --- Configuration properties to control mock behavior ---
    override var simulateError: KMPError? = null
    override var signedInUserOverride: FirebaseUser? = null
    override var nextUserToReturnOnAuth: FirebaseUser? = null // Specify the exact user for next signIn/signUp success
    override var userDatabase: MutableMap<String, Map<String, Any>> = mutableMapOf()
    override var shouldSignOutSucceed: Boolean = true
    override var autoSignInOnAuthSuccess: Boolean = true

    // --- Captured interactions for verification ---
    private val _loggedEvents = mutableListOf<Pair<String, Map<String, Any>?>>()
    val loggedEvents: List<Pair<String, Map<String, Any>?>> get() = _loggedEvents

    private val _userProperties = mutableMapOf<String, String>()
    val userProperties: Map<String, String> get() = _userProperties

    private var _currentAnalyticsUserId: String? = null
    val currentAnalyticsUserId: String? get() = _currentAnalyticsUserId

    var configureCalled: Boolean = false
        private set

    /**
     * Resets the mock to its default state. Call this before each test.
     */
    override fun reset() {
        simulateError = null
        signedInUserOverride = null
        nextUserToReturnOnAuth = null
        userDatabase.clear()
        shouldSignOutSucceed = true
        autoSignInOnAuthSuccess = true
        _loggedEvents.clear()
        _userProperties.clear()
        _currentAnalyticsUserId = null
        configureCalled = false
        println("MockFirebaseKMP: Reset to default state.")
    }

    override fun configure() {
        configureCalled = true
        println("MockFirebaseKMP: configure() called.")
    }

    override fun isUserSignedIn(): Boolean {
        val isSignedIn = signedInUserOverride != null
        println("MockFirebaseKMP: isUserSignedIn() called, returning $isSignedIn.")
        return isSignedIn
    }

    override fun signIn(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit) {
        println("MockFirebaseKMP: signIn(email: $email) called.")
        if (simulateError != null) {
            println("MockFirebaseKMP: Simulating signIn error: ${simulateError!!}")
            completion(null, simulateError)
            return
        }

        val userToReturn = nextUserToReturnOnAuth ?: FirebaseUser(
            uid = "mockUID_signIn_${email.hashCode()}",
            email = email,
            displayName = "Mock SignedIn User (Default)",
            photoURL = null
        )
        // Clear nextUserToReturnOnAuth after use if you want it to be a one-time setting
        // nextUserToReturnOnAuth = null

        if (autoSignInOnAuthSuccess) {
            signedInUserOverride = userToReturn
        }
        println("MockFirebaseKMP: signIn successful. User: ${userToReturn.uid}. Auto sign-in: $autoSignInOnAuthSuccess")
        completion(userToReturn, null)
    }

    override fun signUp(email: String, password: String, completion: (FirebaseUser?, KMPError?) -> Unit) {
        println("MockFirebaseKMP: signUp(email: $email) called.")
        if (simulateError != null) {
            println("MockFirebaseKMP: Simulating signUp error: ${simulateError!!}")
            completion(null, simulateError)
            return
        }

        val userToReturn = nextUserToReturnOnAuth ?: FirebaseUser(
            uid = "mockUID_signUp_${email.hashCode()}",
            email = email,
            displayName = "New Mock User (Default)",
            photoURL = null
        )
        // Clear nextUserToReturnOnAuth after use
        // nextUserToReturnOnAuth = null

        if (autoSignInOnAuthSuccess) {
            signedInUserOverride = userToReturn
        }
        println("MockFirebaseKMP: signUp successful. New user: ${userToReturn.uid}. Auto sign-in: $autoSignInOnAuthSuccess")
        completion(userToReturn, null)
    }

    override fun signOut(): Boolean {
        println("MockFirebaseKMP: signOut() called.")
        if (simulateError != null) {
            println("MockFirebaseKMP: Simulating signOut error (returning false as per simulateError).")
            return false
        }
        if (shouldSignOutSucceed) {
            signedInUserOverride = null
            println("MockFirebaseKMP: signOut successful.")
            return true
        } else {
            println("MockFirebaseKMP: signOut failed as per 'shouldSignOutSucceed' configuration.")
            return false
        }
    }

    override fun saveUser(userId: String, userData: Map<String, Any>, completion: (KMPError?) -> Unit) {
        println("MockFirebaseKMP: saveUser(userId: $userId, data: $userData) called.")
        if (simulateError != null) {
            println("MockFirebaseKMP: Simulating saveUser error: ${simulateError!!}")
            completion(simulateError)
            return
        }
        userDatabase[userId] = userData
        println("MockFirebaseKMP: User data saved for $userId.")
        completion(null)
    }

    override fun getUser(userId: String, completion: (Map<String, Any>?, KMPError?) -> Unit) {
        println("MockFirebaseKMP: getUser(userId: $userId) called.")
        if (simulateError != null) {
            println("MockFirebaseKMP: Simulating getUser error: ${simulateError!!}")
            completion(null, simulateError)
            return
        }
        val data = userDatabase[userId]
        if (data != null) {
            println("MockFirebaseKMP: User data found for $userId: $data")
            completion(data, null)
        } else {
            println("MockFirebaseKMP: No user data found for $userId. Returning null data, null error.")
            completion(null, null)
        }
    }

    override fun logEvent(name: String, parameters: Map<String, Any>?) {
        println("MockFirebaseKMP: logEvent(name: $name, parameters: $parameters) called.")
        _loggedEvents.add(name to parameters)
    }

    override fun setUserProperty(name: String, value: String) {
        println("MockFirebaseKMP: setUserProperty(name: $name, value: $value) called.")
        _userProperties[name] = value
    }

    override fun setUserId(userId: String) {
        println("MockFirebaseKMP: setUserId(userId: $userId) called.")
        _currentAnalyticsUserId = userId
    }

    override fun getCurrentUser(): FirebaseUser? {
        println("MockFirebaseKMP: getCurrentUser() called, returning ${signedInUserOverride?.uid ?: "null"}.")
        return signedInUserOverride
    }
}