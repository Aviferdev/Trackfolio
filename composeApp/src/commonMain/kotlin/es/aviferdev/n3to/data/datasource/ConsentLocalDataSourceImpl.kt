package es.aviferdev.n3to.data.datasource

import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.ConsentPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Implementación de [ConsentLocalDataSource] sobre SQLDelight.
 *
 * Reemplaza la anterior implementación basada en AppSettings (clave-valor),
 * aportando integridad y homogeneidad con el resto del stack de persistencia.
 */
class ConsentLocalDataSourceImpl(
    private val database: N3toDatabase
) : ConsentLocalDataSource {

    private val queries = database.consentPreferencesQueries

    override suspend fun load(): ConsentPreferences = withContext(Dispatchers.IO) {
        queries.select().executeAsOneOrNull()?.toDomain()
            ?: ConsentPreferences() // valores por defecto si no hay fila
    }

    override suspend fun save(prefs: ConsentPreferences) = withContext(Dispatchers.IO) {
        val e = prefs.toEntity()
        queries.insertOrReplace(
            analytics = e.analytics,
            crashReporting = e.crashReporting,
            consentVersion = e.consentVersion,
            consentTimestamp = e.consentTimestamp,
            hasDecided = e.hasDecided
        )
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        queries.deleteAll()
    }
}
