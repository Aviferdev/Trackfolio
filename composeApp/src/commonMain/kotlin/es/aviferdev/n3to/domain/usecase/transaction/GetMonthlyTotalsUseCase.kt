package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetMonthlyTotalsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String, month: String): Flow<MonthlyTotals> =
        repository.getMonthlyTotalsByAccount(accountId, year, month)
}
