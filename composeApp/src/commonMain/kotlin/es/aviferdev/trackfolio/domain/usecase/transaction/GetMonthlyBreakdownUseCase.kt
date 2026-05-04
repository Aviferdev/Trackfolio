package es.aviferdev.trackfolio.domain.usecase.transaction

import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetMonthlyBreakdownUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String): Flow<List<MonthlyTotals>> =
        repository.getMonthlyBreakdown(accountId, year)
}
