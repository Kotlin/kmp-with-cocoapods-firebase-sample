package org.jetbrains.kotlin.firebase.sample.samples

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRInAppMessaging

/**
 * Sample demonstrating Firebase In-App Messaging:
 * managing contextual messages displayed to users within the app.
 */
@Suppress("UNUSED")
object InAppMessagingSample {

    private val inAppMessaging: FIRInAppMessaging
        get() = FIRInAppMessaging.inAppMessaging()

    /**
     * Enable or disable automatic data collection for In-App Messaging.
     * When disabled, messages won't be fetched or displayed automatically.
     */
    fun setAutomaticDataCollectionEnabled(enabled: Boolean) {
        inAppMessaging.setAutomaticDataCollectionEnabled(enabled)
    }

    /**
     * Enable or disable message display.
     * When suppressed, messages are still fetched but not rendered.
     * Useful during onboarding flows or time-critical operations.
     */
    fun setMessagesSuppressed(suppressed: Boolean) {
        inAppMessaging.setMessageDisplaySuppressed(suppressed)
    }

    /**
     * Trigger an in-app message for a specific analytics event.
     * Messages configured in the Firebase console with matching trigger
     * events will be displayed.
     */
    fun triggerEvent(eventName: String) {
        inAppMessaging.triggerEvent(eventName)
    }
}
