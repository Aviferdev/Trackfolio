package es.aviferdev.n3to.data.repository.budget

import es.aviferdev.n3to.data.database.CategoryBudgetEntity
import es.aviferdev.n3to.data.datasource.budget.CategoryBudgetLocalDataSource
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.repository.CategoryBudgetRepository
import kotlinx.coroutines.flow.Flow

class CategoryBudgetRepositoryImpl(
    private val dataSource: CategoryBudgetLocalDataSource
) : CategoryBudgetRepository {

    override fun getBudgetByCategoryId(categoryId: String): Flow<CategoryBudgetEntity?> =
        dataSource.getByCategoryId(categoryId)

    override fun getBudgetsByAccount(accountId: String): Flow<List<CategoryBudgetEntity>> =
        dataSource.getByAccount(accountId)

    override suspend fun saveBudget(
        categoryId: String,
        annualLimit: Double,
        limitType: LimitType
    ): Result<Unit> =
        dataSource.upsert(categoryId, annualLimit, limitType.name)

    override suspend fun deleteBudget(categoryId: String): Result<Unit> =
        dataSource.delete(categoryId)
}
