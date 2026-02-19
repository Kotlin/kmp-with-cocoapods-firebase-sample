package org.jetbrains.kotlin.firebase.sample

import cocoapods.FirebaseAuth.FIRUser
import platform.Foundation.NSError

internal fun FIRUser.toKMPUser(): FirebaseUser {
    return FirebaseUser(
        uid = this.uid(),
        email = this.email() ?: "",
        displayName = this.displayName(),
        photoURL = this.photoURL()?.absoluteString
    )
}

internal fun NSError.toKMPError(): KMPError {
    return KMPError(this)
}
