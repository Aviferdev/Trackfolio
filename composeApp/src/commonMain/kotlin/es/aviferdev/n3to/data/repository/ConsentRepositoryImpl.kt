package es.aviferdev.n3to.data.repository

import es.aviferdev.n3to.data.datasource.ConsentLocalDataSource
import es.aviferdev.n3to.domain.model.ConsentPreferences
import es.aviferdev.n3to.domain.repository.ConsentRepository

class ConsentRepositoryImpl(
    private val localDataSource: ConsentLocalDataSource
) : ConsentRepository {

    override suspend fun getConsent(): ConsentPreferences = localDataSource.load()

    override suspend fun saveConsent(prefs: ConsentPreferences) = localDataSource.save(prefs)

    override suspend fun hasUserDecided(): Boolean = localDataSource.load().hasDecided

    override suspend fun revokeAll() = localDataSource.clear()
}
