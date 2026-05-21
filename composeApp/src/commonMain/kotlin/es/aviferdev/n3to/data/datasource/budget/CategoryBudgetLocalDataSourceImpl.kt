package es.aviferdev.n3to.data.datasource.budget

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.CategoryBudgetEntity
import es.aviferdev.n3to.data.database.N3toDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoryBudgetLocalDataSourceImpl(
    private val database: N3toDatabase
) : CategoryBudgetLocalDataSource {

    private val queries = database.categoryBudgetQueries

    override fun getByCategoryId(categoryId: String): Flow<CategoryBudgetEntity?> =
        queries.selectByCategoryId(categoryId).asFlow().mapToOneOrNull(Dispatchers.IO)

    override fun getByAccount(accountId: String): Flow<List<CategoryBudgetEntity>> =
        queries.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO)

    override suspend fun upsert(
        categoryId: String,
        annualLimit: Double,
        limitType: String
    ): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.upsert(categoryId, annualLimit, limitType)
            }
        }

    override suspend fun delete(categoryId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.deleteByCategoryId(categoryId)
            }
        }
}
