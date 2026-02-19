package org.jetbrains.kotlin.firebase.sample.samples

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRCrashlytics
import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRExceptionModel
import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRStackFrame
import platform.Foundation.NSError
import platform.Foundation.NSNumber

/**
 * Sample demonstrating Firebase Crashlytics:
 * logging errors, custom keys, and non-fatal exceptions.
 */
@Suppress("UNUSED")
object CrashlyticsSample {

    private val crashlytics: FIRCrashlytics
        get() = FIRCrashlytics.crashlytics()

    /**
     * Enable or disable Crashlytics data collection.
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    /**
     * Set a user identifier for crash reports.
     */
    fun setUserId(userId: String) {
        crashlytics.setUserID(userId)
    }

    /**
     * Set a custom key-value pair for crash reports.
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomValue(value, key)
    }

    /**
     * Set a custom key with a numeric value.
     */
    fun setCustomIntKey(key: String, value: Int) {
        crashlytics.setCustomValue(NSNumber(int = value), key)
    }

    /**
     * Log a message that will be included in the next crash report.
     */
    fun log(message: String) {
        crashlytics.log(message)
    }

    /**
     * Record a non-fatal error.
     */
    fun recordError(error: NSError) {
        crashlytics.recordError(error)
    }

    /**
     * Record a custom non-fatal exception with stack frames.
     */
    fun recordCustomException(name: String, reason: String) {
        val exceptionModel = FIRExceptionModel(name = name, reason = reason)
        exceptionModel.setStackTrace(
            listOf(
                FIRStackFrame(symbol = "sampleFunction()", file = "Sample.kt", line = 42)
            )
        )
        crashlytics.recordExceptionModel(exceptionModel)
    }

    /**
     * Check if there were unsent crash reports from a previous session.
     */
    fun checkUnsentReports(completion: (Boolean) -> Unit) {
        crashlytics.checkForUnsentReportsWithCompletion { hasReports ->
            completion(hasReports)
        }
    }
}
