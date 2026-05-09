package es.aviferdev.trackfolio.domain.usecase.transaction

import es.aviferdev.trackfolio.domain.model.CategoryBreakdown
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetExpensesByCategoryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String): Flow<List<CategoryBreakdown>> =
        repository.getExpensesByCategoryPerYear(accountId, year)
}