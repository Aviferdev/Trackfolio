package es.aviferdev.n3to.data.datasource.taxprofile

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.TaxProfileSnapshotEntity
import es.aviferdev.n3to.domain.model.TaxProfile
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class TaxProfileSnapshotLocalDataSourceImpl(
    private val database: N3toDatabase
) : TaxProfileSnapshotLocalDataSource {

    private val queries = database.taxProfileSnapshotQueries

    override fun getAll(): Flow<List<TaxProfileSnapshot>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getActive(date: LocalDate): TaxProfileSnapshot? =
        withContext(Dispatchers.IO) {
            queries.selectActive(date.toString()).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun insert(
        id: String,
        countryCode: String?,
        currency: String,
        effectiveFrom: LocalDate
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insert(id, countryCode, currency, effectiveFrom.toString())
        }
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.delete(id)
        }
    }

    private fun TaxProfileSnapshotEntity.toDomain(): TaxProfileSnapshot {
        val profile = TaxProfile.ALL.find { it.countryCode == countryCode }
            ?: TaxProfile.CUSTOM.copy(currency = currency)
        return TaxProfileSnapshot(
            id = id,
            profile = profile,
            effectiveFrom = LocalDate.parse(effectiveFrom)
        )
    }
}
