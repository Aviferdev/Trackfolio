package es.aviferdev.n3to.domain.usecase.assettransaction

import es.aviferdev.n3to.domain.model.MonthlyInvestment
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import kotlinx.coroutines.flow.Flow

class GetMonthlyInvestmentsUseCase(private val repository: AssetTransactionRepository) {
    operator fun invoke(accountId: String, year: String): Flow<List<MonthlyInvestment>> =
        repository.getMonthlyInvestmentsByYear(accountId, year)
}