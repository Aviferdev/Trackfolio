package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.SavingsRate
import es.aviferdev.n3to.domain.model.SavingsRateType

interface SavingsRatesRepository {
    suspend fun getShortTerm(forceRefresh: Boolean = false): Result<List<SavingsRate>>
    suspend fun getMediumTerm(forceRefresh: Boolean = false): Result<List<SavingsRate>>
    fun getLastFetchedAt(type: SavingsRateType): Long?
}
