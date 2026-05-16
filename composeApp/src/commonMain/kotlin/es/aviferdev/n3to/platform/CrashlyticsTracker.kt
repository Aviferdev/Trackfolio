package es.aviferdev.n3to.platform

expect class CrashlyticsTracker {
    fun setCrashReportingEnabled(enabled: Boolean)
    fun recordException(throwable: Throwable)
    fun log(message: String)
    fun setUserId(userId: String?)
    fun setCustomKey(key: String, value: String)
}
