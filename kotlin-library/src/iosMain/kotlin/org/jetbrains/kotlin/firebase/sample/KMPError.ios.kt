package org.jetbrains.kotlin.firebase.sample

 import platform.Foundation.NSError

@Suppress("UNUSED")
actual class KMPError(val nsError: NSError) {
    actual val domain: KMPErrorDomain?
        get() = nsError.domain()

    actual val code: Int
        get() = nsError.code.toInt()

    actual val userInfo: Map<Any?, *>
        get() = nsError.userInfo

    actual val localizedDescription: String get() = nsError.localizedDescription()
}