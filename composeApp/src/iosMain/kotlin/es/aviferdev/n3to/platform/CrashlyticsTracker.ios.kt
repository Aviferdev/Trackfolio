package es.aviferdev.n3to.platform

actual class CrashlyticsTracker {
    actual fun setCrashReportingEnabled(enabled: Boolean) {
        // iOS: FirebaseCrashlyticsCollectionEnabled se configura en Info.plist
        // El cambio en tiempo de ejecución se maneja vía FIRCrashlytics
    }

    actual fun recordException(throwable: Throwable) {
        // throwable.toNSError() + FIRCrashlytics.crashlytics().recordError()
    }

    actual fun log(message: String) {
        // FIRCrashlytics.crashlytics().log(message)
    }

    actual fun setUserId(userId: String?) {
        // FIRCrashlytics.crashlytics().setUserID(userId)
    }

    actual fun setCustomKey(key: String, value: String) {
        // FIRCrashlytics.crashlytics().setCustomValue(value, forKey: key)
    }
}
