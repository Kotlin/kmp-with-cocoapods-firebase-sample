package org.jetbrains.kotlin.firebase.sample

// Data class for user information
data class FirebaseUser(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val photoURL: String? = null
)