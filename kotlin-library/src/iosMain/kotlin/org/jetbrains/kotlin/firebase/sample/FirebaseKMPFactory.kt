package org.jetbrains.kotlin.firebase.sample

/**
 * Factory object for creating Firebase KMP instances
 */
@Suppress("UNUSED")
object FirebaseKMPFactory {
    /**
     * Creates and returns an instance of the `FirebaseKMP` interface.
     * This instance provides access to Firebase operations and functionality.
     *
     * @return An implementation of `FirebaseKMP`.
     */
    fun firebase(): FirebaseKMP = FirebaseKMPImpl()

    /**
     * Creates and returns an instance of a mock implementation of the `FirebaseKMP` interface.
     *
     * This method provides a `MockFirebaseKMP` object, which is a mock implementation of
     * the `FirebaseKMP` interface. It can be used for testing Firebase-related functionality
     * without requiring actual interaction with Firebase services.
     *
     * @return An instance of `IMockFirebaseKMP` implemented by `MockFirebaseKMP`.
     */
    fun mock(): MockFirebaseKMP = MockFirebaseKMPImpl()
}