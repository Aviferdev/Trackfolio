package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.ConsentPreferences

interface ConsentRepository {
    suspend fun getConsent(): ConsentPreferences
    suspend fun saveConsent(prefs: ConsentPreferences)
    suspend fun hasUserDecided(): Boolean
    suspend fun revokeAll()
}
