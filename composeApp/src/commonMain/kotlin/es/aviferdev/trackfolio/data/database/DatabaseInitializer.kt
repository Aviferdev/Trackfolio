package es.aviferdev.trackfolio.data.database

import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.data.datasource.AccountLocalDataSource
import es.aviferdev.trackfolio.data.datasource.CategoryLocalDataSource
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.AccountType
import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

class DatabaseInitializer(
    private val categoryDataSource: CategoryLocalDataSource,
    private val accountDataSource: AccountLocalDataSource
) {
    suspend fun initializeIfNeeded() {
        val categoryCount = categoryDataSource.count().first()
        if (categoryCount == 0L) insertDefaultCategories()

        val accounts = accountDataSource.getAll().first()
        if (accounts.isEmpty()) insertDefaultAccount()
    }

    private suspend fun insertDefaultCategories() {
        defaultCategories().forEach { category ->
            categoryDataSource.insert(category.toEntity())
        }
    }

    private suspend fun insertDefaultAccount() {
        val account = Account(
            id = DEFAULT_ACCOUNT_ID,
            name = "Mi cuenta",
            type = AccountType.CASH,
            currency = "EUR",
            balance = 0.0,
            createdAt = Clock.System.now().toEpochMilliseconds()
        )
        accountDataSource.insert(account.toEntity())
    }

    private fun defaultCategories(): List<Category> = listOf(
        Category(id = "cat_exp_01", name = "Alimentación", type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_02", name = "Transporte",   type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_03", name = "Hogar",        type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_04", name = "Salud",        type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_05", name = "Ocio",         type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_06", name = "Ropa",         type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_07", name = "Educación",    type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_exp_08", name = "Otros",        type = TransactionType.EXPENSE, isDefault = true),
        Category(id = "cat_inc_01", name = "Salario",      type = TransactionType.INCOME,  isDefault = true),
        Category(id = "cat_inc_02", name = "Freelance",    type = TransactionType.INCOME,  isDefault = true),
        Category(id = "cat_inc_03", name = "Inversiones",  type = TransactionType.INCOME,  isDefault = true),
        Category(id = "cat_inc_04", name = "Regalo",       type = TransactionType.INCOME,  isDefault = true),
        Category(id = "cat_inc_05", name = "Reembolso",    type = TransactionType.INCOME,  isDefault = true),
        Category(id = "cat_inc_06", name = "Otros",        type = TransactionType.INCOME,  isDefault = true),
    )

    companion object {
        const val DEFAULT_ACCOUNT_ID = "account_default"
    }
}
