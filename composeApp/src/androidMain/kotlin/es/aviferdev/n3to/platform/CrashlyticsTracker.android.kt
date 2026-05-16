package es.aviferdev.n3to.platform

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

actual class CrashlyticsTracker {
    private val crashlytics: FirebaseCrashlytics = Firebase.crashlytics

    actual fun setCrashReportingEnabled(enabled: Boolean) {
        crashlytics.isCrashlyticsCollectionEnabled = enabled
    }

    actual fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    actual fun log(message: String) {
        crashlytics.log(message)
    }

    actual fun setUserId(userId: String?) {
        crashlytics.setUserId(userId ?: "")
    }

    actual fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
}
