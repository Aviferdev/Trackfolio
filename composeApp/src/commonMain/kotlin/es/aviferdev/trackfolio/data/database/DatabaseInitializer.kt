package es.aviferdev.trackfolio.data.database

import es.aviferdev.trackfolio.data.datasource.CategoryLocalDataSource
import es.aviferdev.trackfolio.data.datasource.AccountLocalDataSource
import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.TransactionType
import kotlinx.coroutines.flow.first

class DatabaseInitializer(
    private val categoryDataSource: CategoryLocalDataSource,
    private val accountDataSource: AccountLocalDataSource
) {
    companion object {
        const val DEFAULT_ACCOUNT_ID = "account_default"
    }

    suspend fun initializeIfNeeded() {
        val categoryCount = categoryDataSource.count().first()
        if (categoryCount == 0L) insertDefaultCategories()
    }

    private suspend fun insertDefaultCategories() {
        defaultCategories().forEach { category ->
            categoryDataSource.insert(
                CategoryEntity(
                    id        = category.id,
                    name      = category.name,
                    type      = category.type.name,
                    isDefault = if (category.isDefault) 1L else 0L,
                    archived  = 0L
                )
            )
        }
    }

    /**
     * Solo categorías de GASTO. Los ingresos ahora usan [IncomeType] (enum fijo).
     */
    private fun defaultCategories(): List<Category> = listOf(
        Category(id = "cat_exp_01", name = "Alimentación", type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_02", name = "Transporte",   type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_03", name = "Hogar",        type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_04", name = "Salud",        type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_05", name = "Ocio",         type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_06", name = "Ropa",         type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_07", name = "Educación",    type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_08", name = "Otros",        type = TransactionType.EXPENSE, isDefault = true),
    )
}
