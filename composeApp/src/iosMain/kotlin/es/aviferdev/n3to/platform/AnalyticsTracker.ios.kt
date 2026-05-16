package es.aviferdev.n3to.platform

import cocoapods.FirebaseAnalytics.FIRAnalytics
import cocoapods.FirebaseAnalytics.FirebaseAnalytics

actual class AnalyticsTracker {
    actual fun setEnabled(enabled: Boolean) {
        FIRAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

    actual fun logEvent(name: String, params: Map<String, String>) {
        FIRAnalytics.logEventWithName(name, parameters = params)
    }

    actual fun logScreenView(screenName: String, screenClass: String) {
        val params = mapOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName,
            FirebaseAnalytics.Param.SCREEN_CLASS to (screenClass.ifEmpty { screenName })
        )
        FIRAnalytics.logEventWithName(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            parameters = params
        )
    }

    actual fun setUserId(userId: String?) {
        FIRAnalytics.setUserID(userId)
    }

    actual fun setUserProperty(name: String, value: String?) {
        FIRAnalytics.setUserPropertyString(value, forName = name)
    }
}
