package es.aviferdev.n3to.domain.usecase.category

import es.aviferdev.n3to.data.database.DatabaseInitializer
import es.aviferdev.n3to.data.datasource.transaction.TransactionCategoryLocalDataSource

class SeedDefaultCategoriesUseCase(
    private val dataSource: TransactionCategoryLocalDataSource
) {
    suspend operator fun invoke(accountId: String) {
        DatabaseInitializer.defaultExpenseCategories(accountId).forEach { category ->
            dataSource.insert(category)
        }
    }
}
