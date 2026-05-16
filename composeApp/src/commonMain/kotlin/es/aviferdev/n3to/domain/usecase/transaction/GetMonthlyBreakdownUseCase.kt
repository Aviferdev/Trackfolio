package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetMonthlyBreakdownUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String): Flow<List<MonthlyTotals>> =
        repository.getMonthlyBreakdown(accountId, year)
}
