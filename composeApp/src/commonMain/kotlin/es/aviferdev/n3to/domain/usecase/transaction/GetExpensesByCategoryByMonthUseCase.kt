package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.CategoryBreakdown
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetExpensesByCategoryByMonthUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String, month: String): Flow<List<CategoryBreakdown>> =
        repository.getExpensesByCategoryPerMonth(accountId, year, month)
}
