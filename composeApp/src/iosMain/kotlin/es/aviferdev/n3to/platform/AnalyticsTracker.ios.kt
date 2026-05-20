@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package es.aviferdev.n3to.platform

import cocoapods.FirebaseAnalytics.FIRAnalytics

actual class AnalyticsTracker {
    actual fun setEnabled(enabled: Boolean) {
        FIRAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    actual fun logEvent(name: String, params: Map<String, String>) {
        FIRAnalytics.logEventWithName(name, parameters = params as Map<Any?, *>)
    }

    actual fun logScreenView(screenName: String, screenClass: String) {
        FIRAnalytics.logEventWithName(
            "screen_view",
            parameters = mapOf<Any?, Any>(
                "screen_name" to screenName,
                "screen_class" to screenClass.ifEmpty { screenName }
            )
        )
    }

    actual fun setUserId(userId: String?) {
        FIRAnalytics.setUserID(userId)
    }

    actual fun setUserProperty(name: String, value: String?) {
        FIRAnalytics.setUserPropertyString(value, forName = name)
    }
}
