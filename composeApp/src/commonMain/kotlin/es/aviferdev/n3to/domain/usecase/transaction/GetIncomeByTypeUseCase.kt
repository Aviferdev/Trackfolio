package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.IncomeTypeBreakdown
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetIncomeByTypeUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String): Flow<List<IncomeTypeBreakdown>> =
        repository.getIncomeByTypePerYear(accountId, year)
}