package org.jetbrains.kotlin.firebase.sample.services

import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRAnalytics

internal class AnalyticsService {

    fun enableCollection() {
        FIRAnalytics.setAnalyticsCollectionEnabled(true)
    }

    @Suppress("UNCHECKED_CAST")
    fun logEvent(name: String, parameters: Map<String, Any>?) {
        val swiftParameters = parameters?.let { it as Map<Any?, *> }
        FIRAnalytics.logEventWithName(name, swiftParameters)
    }

    fun setUserProperty(name: String, value: String) {
        FIRAnalytics.setUserPropertyString(value, name)
    }

    fun setUserId(userId: String) {
        FIRAnalytics.setUserID(userId)
    }
}
