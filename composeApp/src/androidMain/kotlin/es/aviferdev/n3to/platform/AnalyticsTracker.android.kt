package es.aviferdev.n3to.platform

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

actual class AnalyticsTracker {
    private val analytics: FirebaseAnalytics = Firebase.analytics

    actual fun setEnabled(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    actual fun logEvent(name: String, params: Map<String, String>) {
        analytics.logEvent(name) {
            params.forEach { (key, value) -> param(key, value) }
        }
    }

    actual fun logScreenView(screenName: String, screenClass: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.ifEmpty { screenName })
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    actual fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }

    actual fun setUserProperty(name: String, value: String?) {
        analytics.setUserProperty(name, value)
    }
}
