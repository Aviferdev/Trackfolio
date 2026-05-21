package es.aviferdev.n3to.data.datasource.taxprofile

import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface TaxProfileSnapshotLocalDataSource {
    fun getAll(): Flow<List<TaxProfileSnapshot>>
    suspend fun getActive(date: LocalDate): TaxProfileSnapshot?
    suspend fun insert(
        id: String,
        countryCode: String?,
        currency: String,
        effectiveFrom: LocalDate
    ): Result<Unit>

    suspend fun delete(id: String): Result<Unit>
}
