package org.jetbrains.kotlin.firebase.sample

/**
 * Typealias for an error domain.
 * This allows common code to use a more descriptive type name.
 */
typealias KMPErrorDomain = String

/**
 * Represents an error in a Kotlin Multiplatform context.
 * The `KMPError` class is a platform-agnostic abstraction of errors, providing a standardized interface for accessing
 * error details such as domain, code, user information, and localized descriptions.
 */
@Suppress("UNUSED")
expect class KMPError {
    /**
     * The domain of the error. For example, "NSURLErrorDomain" or a custom domain.
     */
    val domain: KMPErrorDomain?

    /**
     * The error code.
     */
    val code: Int

    /**
     * A dictionary containing additional information about the error.
     * In Kotlin, this is represented as Map<Any?, *>.
     */
    val userInfo: Map<Any?, *> // Using Any? for keys to match NSError's flexibility

    /**
     * A localized description of the error, often retrieved from userInfo.
     */
    val localizedDescription: String
}