package es.aviferdev.n3to.data.datasource.budget

import es.aviferdev.n3to.data.database.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

interface CategoryBudgetLocalDataSource {
    fun getByCategoryId(categoryId: String): Flow<CategoryBudgetEntity?>
    fun getByAccount(accountId: String): Flow<List<CategoryBudgetEntity>>
    suspend fun upsert(categoryId: String, annualLimit: Double, limitType: String): Result<Unit>
    suspend fun delete(categoryId: String): Result<Unit>
}
