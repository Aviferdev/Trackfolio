package es.aviferdev.trackfolio.data.database

import es.aviferdev.trackfolio.data.datasource.asset.AssetCategoryLocalDataSource
import es.aviferdev.trackfolio.data.datasource.transaction.TransactionCategoryLocalDataSource
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.TransactionType
import kotlinx.coroutines.flow.firstOrNull

class DatabaseInitializer(
    private val transactionCategoryDataSource: TransactionCategoryLocalDataSource,
    private val assetCategoryDataSource: AssetCategoryLocalDataSource
) {
    companion object {

        /** ID fijo de la categoría de ajuste de saldo. Usado por ReconcileBalanceUseCase. */
        const val ADJUSTMENT_CATEGORY_ID = "cat_adj_reconciliation"

        val DEFAULT_ASSET_CATEGORIES = listOf(
            AssetCategory(id = "fixed_cat_stocks",       name = "Acciones",              icon = "📊", sortOrder = 0, createdAt = 0),
            AssetCategory(id = "fixed_cat_etfs",         name = "ETFs",                  icon = "📈", sortOrder = 1, createdAt = 0),
            AssetCategory(id = "fixed_cat_funds",        name = "Fondos de inversión",   icon = "💼", sortOrder = 2, createdAt = 0),
            AssetCategory(id = "fixed_cat_crypto",       name = "Criptomonedas",         icon = "₿",  sortOrder = 3, createdAt = 0),
            AssetCategory(id = "fixed_cat_bonds",        name = "Bonos / Deuda pública", icon = "📜", sortOrder = 4, createdAt = 0),
            AssetCategory(id = "fixed_cat_deposits",     name = "Depósitos bancarios",   icon = "🏦", sortOrder = 5, createdAt = 0),
            AssetCategory(id = "fixed_cat_pensions",     name = "Planes de pensiones",   icon = "🛡", sortOrder = 6, createdAt = 0),
            AssetCategory(id = "fixed_cat_commodities",  name = "Materias primas",       icon = "🪙", sortOrder = 7, createdAt = 0),
            AssetCategory(id = "fixed_cat_crowdlending", name = "Crowdlending",          icon = "🤝", sortOrder = 8, createdAt = 0),
        )

        val DEFAULT_EXPENSE_CATEGORIES = listOf(
            CategoryEntity(id = "cat_exp_01", name = "Alimentación", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_02", name = "Transporte",   type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_03", name = "Hogar",        type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_04", name = "Salud",        type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_05", name = "Ocio",         type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_06", name = "Ropa",         type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_07", name = "Educación",    type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_08", name = "Otros",        type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
        )

        /** Categoría especial para transacciones de ajuste / reconciliación. */
        val ADJUSTMENT_CATEGORY = CategoryEntity(
            id        = ADJUSTMENT_CATEGORY_ID,
            name      = "Ajuste de saldo",
            type      = TransactionType.ADJUSTMENT.name,
            isDefault = 1L,
            archived  = 0L
        )
    }

    suspend fun initializeIfNeeded() {
        if (transactionCategoryDataSource.count().firstOrNull() == 0L) {
            DEFAULT_EXPENSE_CATEGORIES.forEach { transactionCategoryDataSource.insert(it) }
        }
        // Siempre intentar insertar la categoría de ajuste (INSERT OR IGNORE)
        transactionCategoryDataSource.insert(ADJUSTMENT_CATEGORY)

        if (assetCategoryDataSource.count().firstOrNull() == 0L) {
            DEFAULT_ASSET_CATEGORIES.forEach { assetCategoryDataSource.insert(it) }
        }
    }
}
