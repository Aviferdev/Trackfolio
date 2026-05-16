package es.aviferdev.n3to.data.datasource

import es.aviferdev.n3to.domain.model.ConsentPreferences

interface ConsentLocalDataSource {
    suspend fun load(): ConsentPreferences
    suspend fun save(prefs: ConsentPreferences)
    suspend fun clear()
}
