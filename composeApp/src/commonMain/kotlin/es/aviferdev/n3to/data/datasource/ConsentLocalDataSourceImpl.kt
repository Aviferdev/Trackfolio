package es.aviferdev.n3to.data.datasource

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.domain.model.ConsentPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConsentLocalDataSourceImpl(
    private val settings: AppSettings
) : ConsentLocalDataSource {

    companion object {
        private const val KEY_ANALYTICS = "consent_analytics"
        private const val KEY_CRASH = "consent_crash_reporting"
        private const val KEY_VERSION = "consent_version"
        private const val KEY_TIMESTAMP = "consent_timestamp"
        private const val KEY_HAS_DECIDED = "consent_has_decided"
    }

    override suspend fun load(): ConsentPreferences = withContext(Dispatchers.Default) {
        ConsentPreferences(
            analytics = settings.getBool(KEY_ANALYTICS, false),
            crashReporting = settings.getBool(KEY_CRASH, false),
            consentVersion = settings.getInt(KEY_VERSION, 0),
            consentTimestamp = settings.getLong(KEY_TIMESTAMP, 0L).takeIf { it > 0 },
            hasDecided = settings.getBool(KEY_HAS_DECIDED, false)
        )
    }

    override suspend fun save(prefs: ConsentPreferences) = withContext(Dispatchers.Default) {
        settings.putBool(KEY_ANALYTICS, prefs.analytics)
        settings.putBool(KEY_CRASH, prefs.crashReporting)
        settings.putInt(KEY_VERSION, prefs.consentVersion)
        prefs.consentTimestamp?.let { settings.putLong(KEY_TIMESTAMP, it) }
        settings.putBool(KEY_HAS_DECIDED, prefs.hasDecided)
    }

    override suspend fun clear() = withContext(Dispatchers.Default) {
        settings.putBool(KEY_ANALYTICS, false)
        settings.putBool(KEY_CRASH, false)
        settings.putInt(KEY_VERSION, 0)
        settings.putLong(KEY_TIMESTAMP, 0L)
        settings.putBool(KEY_HAS_DECIDED, false)
    }
}
