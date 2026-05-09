package es.aviferdev.trackfolio.domain.usecase.assettransaction

import es.aviferdev.trackfolio.domain.model.MonthlyInvestment
import es.aviferdev.trackfolio.domain.repository.AssetTransactionRepository
import kotlinx.coroutines.flow.Flow

class GetMonthlyInvestmentsUseCase(private val repository: AssetTransactionRepository) {
    operator fun invoke(accountId: String, year: String): Flow<List<MonthlyInvestment>> =
        repository.getMonthlyInvestmentsByYear(accountId, year)
}