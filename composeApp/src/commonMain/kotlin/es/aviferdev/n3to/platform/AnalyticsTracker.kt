package es.aviferdev.n3to.platform

expect class AnalyticsTracker {
    fun setEnabled(enabled: Boolean)
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
    fun logScreenView(screenName: String, screenClass: String = "")
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
}
