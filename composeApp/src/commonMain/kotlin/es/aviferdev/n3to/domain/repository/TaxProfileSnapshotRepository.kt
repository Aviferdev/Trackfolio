package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.TaxProfile
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface TaxProfileSnapshotRepository {
    fun getAll(): Flow<List<TaxProfileSnapshot>>
    suspend fun getActive(date: LocalDate): TaxProfileSnapshot?
    suspend fun save(profile: TaxProfile, effectiveFrom: LocalDate): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
