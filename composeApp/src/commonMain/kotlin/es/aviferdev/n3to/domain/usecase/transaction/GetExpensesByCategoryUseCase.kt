package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.CategoryBreakdown
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetExpensesByCategoryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String): Flow<List<CategoryBreakdown>> =
        repository.getExpensesByCategoryPerYear(accountId, year)
}