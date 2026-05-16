package es.aviferdev.n3to.domain.model

data class ConsentPreferences(
    val analytics: Boolean = false,
    val crashReporting: Boolean = false,
    val consentVersion: Int = CURRENT_CONSENT_VERSION,
    val consentTimestamp: Long? = null,
    val hasDecided: Boolean = false
) {
    companion object {
        const val CURRENT_CONSENT_VERSION = 1
    }
}
