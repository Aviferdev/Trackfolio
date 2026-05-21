package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.IncomeTypeBreakdown
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetIncomeByTypeByMonthUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String, month: String): Flow<List<IncomeTypeBreakdown>> =
        repository.getIncomeByTypePerMonth(accountId, year, month)
}
