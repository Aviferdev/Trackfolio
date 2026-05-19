package es.aviferdev.n3to.data.datasource.savingsrates

import es.aviferdev.n3to.domain.model.SavingsRate

interface SavingsRatesRemoteDataSource {
    suspend fun fetchShortTerm(): Result<List<SavingsRate>>
    suspend fun fetchMediumTerm(): Result<List<SavingsRate>>
}
