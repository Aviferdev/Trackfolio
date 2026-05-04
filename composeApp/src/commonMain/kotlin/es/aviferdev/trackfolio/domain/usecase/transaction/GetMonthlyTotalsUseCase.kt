package es.aviferdev.trackfolio.domain.usecase.transaction

import es.aviferdev.trackfolio.domain.model.MonthlyTotals
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetMonthlyTotalsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String, month: String): Flow<MonthlyTotals> =
        repository.getMonthlyTotalsByAccount(accountId, year, month)
}
