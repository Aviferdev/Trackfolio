package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.data.database.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

interface CategoryBudgetRepository {
    fun getBudgetByCategoryId(categoryId: String): Flow<CategoryBudgetEntity?>
    fun getBudgetsByAccount(accountId: String): Flow<List<CategoryBudgetEntity>>
    suspend fun saveBudget(
        categoryId: String,
        annualLimit: Double,
        limitType: LimitType
    ): Result<Unit>

    suspend fun deleteBudget(categoryId: String): Result<Unit>
}
