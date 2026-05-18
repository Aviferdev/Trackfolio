package es.aviferdev.n3to.data.database

import es.aviferdev.n3to.data.datasource.account.AccountLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetCategoryLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetPriceHistoryLocalDataSource
import es.aviferdev.n3to.data.datasource.asset.AssetTransactionLocalDataSource
import es.aviferdev.n3to.data.datasource.assetmetadata.AssetMetadataLocalDataSource
import es.aviferdev.n3to.data.datasource.debt.DebtLocalDataSource
import es.aviferdev.n3to.data.datasource.emergencyfund.EmergencyFundLocalDataSource
import es.aviferdev.n3to.data.datasource.goal.GoalLocalDataSource
import es.aviferdev.n3to.data.datasource.loan.LoanLocalDataSource
import es.aviferdev.n3to.data.datasource.loan.LoanRateChangeLocalDataSource
import es.aviferdev.n3to.data.datasource.platform.PlatformLocalDataSource
import es.aviferdev.n3to.data.datasource.realestate.RealEstatePropertyLocalDataSource
import es.aviferdev.n3to.data.datasource.transaction.TransactionCategoryLocalDataSource
import es.aviferdev.n3to.data.datasource.transaction.TransactionLocalDataSource
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetPriceHistory
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.Debt
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.domain.model.EmergencyFund
import es.aviferdev.n3to.domain.model.EmergencyFundMethod
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.LoanRateChange
import es.aviferdev.n3to.domain.model.LoanType
import es.aviferdev.n3to.domain.model.MonthlyGoal
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.PropertyType
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class DatabaseInitializer(
    private val transactionCategoryDataSource: TransactionCategoryLocalDataSource,
    private val assetCategoryDataSource: AssetCategoryLocalDataSource,
    private val assetMetadataDataSource: AssetMetadataLocalDataSource? = null,
    private val accountDataSource: AccountLocalDataSource? = null,
    private val transactionDataSource: TransactionLocalDataSource? = null,
    private val assetDataSource: AssetLocalDataSource? = null,
    private val assetTransactionDataSource: AssetTransactionLocalDataSource? = null,
    private val platformDataSource: PlatformLocalDataSource? = null,
    private val debtDataSource: DebtLocalDataSource? = null,
    private val priceHistoryDataSource: AssetPriceHistoryLocalDataSource? = null,
    private val realEstateDataSource: RealEstatePropertyLocalDataSource? = null,
    private val loanDataSource: LoanLocalDataSource? = null,
    private val loanRateChangeDataSource: LoanRateChangeLocalDataSource? = null,
    private val goalDataSource: GoalLocalDataSource? = null,
    private val emergencyFundDataSource: EmergencyFundLocalDataSource? = null
) {
    companion object {

        /** ID fijo de la categoría de ajuste de saldo. Usado por ReconcileBalanceUseCase. */
        const val ADJUSTMENT_CATEGORY_ID = "cat_adj_reconciliation"

        val DEFAULT_ASSET_CATEGORIES = listOf(
            AssetCategory(
                id = "fixed_cat_stocks",
                name = "Acciones",
                icon = "📊",
                sortOrder = 0,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_etfs",
                name = "ETFs",
                icon = "📈",
                sortOrder = 1,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_funds",
                name = "Fondos de inversión",
                icon = "💼",
                sortOrder = 2,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_crypto",
                name = "Criptomonedas",
                icon = "₿",
                sortOrder = 3,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_pensions",
                name = "Planes de pensiones",
                icon = "🛡",
                sortOrder = 4,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_fixedincome",
                name = "Renta fija",
                icon = "🏦",
                sortOrder = 5,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_commodities",
                name = "Materias primas",
                icon = "🪙",
                sortOrder = 7,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_crowdlending",
                name = "Crowdlending",
                icon = "🤝",
                sortOrder = 8,
                createdAt = 0
            ),
        )

        val DEFAULT_ASSET_SECTORS = listOf(
            AssetSector(id = "sector_tech", name = "Tecnología", icon = "💻", createdAt = 0),
            AssetSector(id = "sector_health", name = "Salud", icon = "🏥", createdAt = 0),
            AssetSector(id = "sector_energy", name = "Energía", icon = "⚡", createdAt = 0),
            AssetSector(id = "sector_finance", name = "Financiero", icon = "🏦", createdAt = 0),
            AssetSector(id = "sector_consumer", name = "Consumo", icon = "🛒", createdAt = 0),
            AssetSector(id = "sector_industrial", name = "Industrial", icon = "🏭", createdAt = 0),
            AssetSector(id = "sector_realestate", name = "Inmobiliario", icon = "🏠", createdAt = 0),
            AssetSector(
                id = "sector_telecom",
                name = "Telecomunicaciones",
                icon = "📡",
                createdAt = 0
            ),
            AssetSector(
                id = "sector_materials",
                name = "Materias Primas",
                icon = "🪙",
                createdAt = 0
            ),
            AssetSector(
                id = "sector_utilities",
                name = "Servicios Públicos",
                icon = "💡",
                createdAt = 0
            ),
        )

        val DEFAULT_ASSET_REGIONS = listOf(
            AssetRegion(id = "region_usa", name = "EE.UU.", createdAt = 0),
            AssetRegion(id = "region_europe", name = "Europa", createdAt = 0),
            AssetRegion(id = "region_asia", name = "Asia", createdAt = 0),
            AssetRegion(id = "region_latam", name = "Latinoamérica", createdAt = 0),
            AssetRegion(id = "region_emerging", name = "Mercados Emergentes", createdAt = 0),
            AssetRegion(id = "region_global", name = "Global", createdAt = 0),
            AssetRegion(id = "region_spain", name = "España", createdAt = 0),
        )

        fun defaultExpenseCategories(accountId: String): List<CategoryEntity> = listOf(
            CategoryEntity(
                id = "cat_exp_01", accountId = accountId,
                name = "Alimentación", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_02", accountId = accountId,
                name = "Transporte", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_03", accountId = accountId,
                name = "Hogar", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_04", accountId = accountId,
                name = "Salud", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_05", accountId = accountId,
                name = "Ocio", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_06", accountId = accountId,
                name = "Ropa", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_07", accountId = accountId,
                name = "Educación", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_08", accountId = accountId,
                name = "Otros", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_loan", accountId = accountId,
                name = "Cuota préstamo", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            // ── Gastos inmobiliarios (compra/venta vivienda) ────
            CategoryEntity(
                id = "cat_exp_prop_notary", accountId = accountId,
                name = "Notaría", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_prop_registry", accountId = accountId,
                name = "Registro", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_prop_tax", accountId = accountId,
                name = "Impuestos (ITP/IVA)", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_prop_agency", accountId = accountId,
                name = "Comisión inmobiliaria", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_prop_appraisal", accountId = accountId,
                name = "Tasación", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_prop_gestoria", accountId = accountId,
                name = "Gestoría", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_prop_municipal", accountId = accountId,
                name = "Plusvalía municipal", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            // ── Gastos de bienes (Valuable: maquinaria, coches, etc.) ──
            CategoryEntity(
                id = "cat_exp_val_transport", accountId = accountId,
                name = "Transporte", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_val_repair", accountId = accountId,
                name = "Reparación", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_val_restoration", accountId = accountId,
                name = "Restauración", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_val_commission", accountId = accountId,
                name = "Comisión", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_val_storage", accountId = accountId,
                name = "Almacenaje", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_val_insurance", accountId = accountId,
                name = "Seguro", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_val_marketing", accountId = accountId,
                name = "Marketing / Publicidad", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
            CategoryEntity(
                id = "cat_exp_val_other", accountId = accountId,
                name = "Otros gastos de bienes", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L
            ),
        )

        /** Categoría especial para transacciones de ajuste / reconciliación. */
        val ADJUSTMENT_CATEGORY = CategoryEntity(
            id = ADJUSTMENT_CATEGORY_ID,
            accountId = "",  // global, compartida entre todas las cuentas
            name = "Ajuste de saldo",
            type = TransactionType.ADJUSTMENT.name,
            isDefault = 1L,
            archived = 0L
        )
    }

    /** Siembra las categorías de gasto por defecto para una cuenta nueva. */
    suspend fun seedDefaultCategoriesForAccount(accountId: String) {
        defaultExpenseCategories(accountId).forEach { transactionCategoryDataSource.insert(it) }
    }

    suspend fun initializeIfNeeded() {
        // Categoría de ajuste (global, sin accountId porque la usan todas las cuentas)
        transactionCategoryDataSource.insert(ADJUSTMENT_CATEGORY)

        if (assetCategoryDataSource.count().firstOrNull() == 0L) {
            DEFAULT_ASSET_CATEGORIES.forEach { assetCategoryDataSource.insert(it) }
        }

        assetMetadataDataSource?.let { metadata ->
            if (metadata.countSectors() == 0L) {
                DEFAULT_ASSET_SECTORS.forEach { metadata.insertSector(it) }
            }
            if (metadata.countRegions() == 0L) {
                DEFAULT_ASSET_REGIONS.forEach { metadata.insertRegion(it) }
            }
        }

        // Insertar datos de prueba si no existen cuentas
        accountDataSource?.let { accountDs ->
            val accountCount = accountDs.count().firstOrNull() ?: 0L
            if (accountCount == 0L) {
                insertTestData()
            }
        }
    }

    private suspend fun insertTestData() {
        val random = Random(42)
        val startTime = 1735689600000L // 2025-01-01 00:00:00 UTC
        val endTime = 1779062400000L   // 2026-05-17 00:00:00 UTC
        val oneDay = 24 * 60 * 60 * 1000L
        val totalDays = ((endTime - startTime) / oneDay)

        listOf(
            Platform(
                id = "plat_ibkr",
                name = "Interactive Brokers",
                icon = "🟢",
                sortOrder = 0,
                createdAt = startTime
            ),
            Platform(
                id = "plat_singular",
                name = "Singular Bank",
                icon = "🏦",
                sortOrder = 1,
                createdAt = startTime
            ),
            Platform(
                id = "plat_ing",
                name = "ING",
                icon = "🟠",
                sortOrder = 2,
                createdAt = startTime
            ),
            Platform(
                id = "plat_binance",
                name = "Binance",
                icon = "🟡",
                sortOrder = 3,
                createdAt = startTime
            ),
            Platform(
                id = "plat_raisin",
                name = "Raisin",
                icon = "🌾",
                sortOrder = 4,
                createdAt = startTime
            )
        ).forEach { platformDataSource?.insert(it) }

        accountDataSource?.insertAccount(
            Account(
                id = "acc_main",
                name = "Cuenta Principal",
                initialBalance = 50000.0,
                computedBalance = 50000.0,
                createdAt = startTime
            )
        )
        // Sembrar categorías de gasto por defecto para la cuenta demo
        seedDefaultCategoriesForAccount("acc_main")

        listOf(
            Asset(
                id = "asset_aapl",
                accountId = "acc_main",
                ticker = "AAPL",
                name = "Apple Inc.",
                notes = "Tech",
                createdAt = startTime + 30 * oneDay,
                assetCategoryId = "fixed_cat_stocks",
                currentPrice = 185.50,
                currentPriceUpdatedAt = endTime
            ),
            Asset(
                id = "asset_msft",
                accountId = "acc_main",
                ticker = "MSFT",
                name = "Microsoft Corporation",
                notes = "Tech",
                createdAt = startTime + 45 * oneDay,
                assetCategoryId = "fixed_cat_stocks",
                currentPrice = 415.20,
                currentPriceUpdatedAt = endTime
            ),
            Asset(
                id = "asset_nvda",
                accountId = "acc_main",
                ticker = "NVDA",
                name = "NVIDIA Corporation",
                notes = "Tech - Semiconductors",
                createdAt = startTime + 60 * oneDay,
                assetCategoryId = "fixed_cat_stocks",
                currentPrice = 875.30,
                currentPriceUpdatedAt = endTime
            ),
            Asset(
                id = "asset_vo",
                accountId = "acc_main",
                ticker = "VO",
                name = "Vanguard Consumer Staples ETF",
                notes = "ETF",
                createdAt = startTime + 75 * oneDay,
                assetCategoryId = "fixed_cat_etfs",
                currentPrice = 245.80,
                currentPriceUpdatedAt = endTime
            ),
            Asset(
                id = "asset_ieac",
                accountId = "acc_main",
                ticker = "IEAC",
                name = "iShares MSCI Europe UCITS",
                notes = "ETF Europa",
                createdAt = startTime + 90 * oneDay,
                assetCategoryId = "fixed_cat_etfs",
                currentPrice = 158.40,
                currentPriceUpdatedAt = endTime
            ),
            Asset(
                id = "asset_btc",
                accountId = "acc_main",
                ticker = "BTC",
                name = "Bitcoin",
                notes = "Crypto",
                createdAt = startTime + 105 * oneDay,
                assetCategoryId = "fixed_cat_crypto",
                currentPrice = 67500.0,
                currentPriceUpdatedAt = endTime
            ),
            Asset(
                id = "asset_eth",
                accountId = "acc_main",
                ticker = "ETH",
                name = "Ethereum",
                notes = "Crypto",
                createdAt = startTime + 120 * oneDay,
                assetCategoryId = "fixed_cat_crypto",
                currentPrice = 3450.0,
                currentPriceUpdatedAt = endTime
            ),
            Asset(
                id = "asset_amundi",
                accountId = "acc_main",
                ticker = "AMUNDI",
                name = "Amundi Europa Stoxx 600",
                notes = "Fondo indexado",
                createdAt = startTime + 135 * oneDay,
                assetCategoryId = "fixed_cat_funds",
                currentPrice = 125.40,
                currentPriceUpdatedAt = endTime
            )
        ).forEach { asset ->
            assetDataSource?.insert(asset)
            if (asset.currentPrice != null && asset.currentPriceUpdatedAt != null) {
                priceHistoryDataSource?.insert(
                    AssetPriceHistory(
                        id = "price_${asset.id}_init",
                        assetId = asset.id,
                        price = asset.currentPrice,
                        recordedAt = asset.currentPriceUpdatedAt
                    )
                )
            }
        }

        val assetTransactions = mutableListOf<AssetTransaction>()
        var atCounter = 1

        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_aapl",
                type = AssetTransactionType.BUY,
                quantity = 50.0,
                pricePerUnit = 145.0,
                date = startTime + 30 * oneDay,
                platformId = "plat_ibkr",
                createdAt = startTime + 30 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_aapl",
                type = AssetTransactionType.BUY,
                quantity = 30.0,
                pricePerUnit = 165.0,
                date = startTime + 200 * oneDay,
                platformId = "plat_ibkr",
                createdAt = startTime + 200 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_aapl",
                type = AssetTransactionType.BUY,
                quantity = 20.0,
                pricePerUnit = 180.0,
                date = startTime + 400 * oneDay,
                platformId = "plat_ibkr",
                createdAt = startTime + 400 * oneDay
            )
        )

        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_msft",
                type = AssetTransactionType.BUY,
                quantity = 40.0,
                pricePerUnit = 350.0,
                date = startTime + 45 * oneDay,
                platformId = "plat_ibkr",
                createdAt = startTime + 45 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_msft",
                type = AssetTransactionType.BUY,
                quantity = 25.0,
                pricePerUnit = 400.0,
                date = startTime + 300 * oneDay,
                platformId = "plat_ibkr",
                createdAt = startTime + 300 * oneDay
            )
        )

        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_nvda",
                type = AssetTransactionType.BUY,
                quantity = 15.0,
                pricePerUnit = 450.0,
                date = startTime + 60 * oneDay,
                platformId = "plat_ibkr",
                createdAt = startTime + 60 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_nvda",
                type = AssetTransactionType.BUY,
                quantity = 10.0,
                pricePerUnit = 650.0,
                date = startTime + 350 * oneDay,
                platformId = "plat_ibkr",
                createdAt = startTime + 350 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_nvda",
                type = AssetTransactionType.SELL,
                quantity = 5.0,
                pricePerUnit = 850.0,
                date = startTime + 470 * oneDay,
                platformId = "plat_ibkr",
                createdAt = startTime + 470 * oneDay
            )
        )

        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_vo",
                type = AssetTransactionType.BUY,
                quantity = 100.0,
                pricePerUnit = 220.0,
                date = startTime + 75 * oneDay,
                platformId = "plat_singular",
                createdAt = startTime + 75 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_ieac",
                type = AssetTransactionType.BUY,
                quantity = 200.0,
                pricePerUnit = 145.0,
                date = startTime + 90 * oneDay,
                platformId = "plat_singular",
                createdAt = startTime + 90 * oneDay
            )
        )

        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_btc",
                type = AssetTransactionType.BUY,
                quantity = 0.5,
                pricePerUnit = 42000.0,
                date = startTime + 105 * oneDay,
                platformId = "plat_binance",
                createdAt = startTime + 105 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_btc",
                type = AssetTransactionType.BUY,
                quantity = 0.25,
                pricePerUnit = 55000.0,
                date = startTime + 400 * oneDay,
                platformId = "plat_binance",
                createdAt = startTime + 400 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_eth",
                type = AssetTransactionType.BUY,
                quantity = 8.0,
                pricePerUnit = 2800.0,
                date = startTime + 120 * oneDay,
                platformId = "plat_binance",
                createdAt = startTime + 120 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_eth",
                type = AssetTransactionType.BUY,
                quantity = 3.0,
                pricePerUnit = 3200.0,
                date = startTime + 450 * oneDay,
                platformId = "plat_binance",
                createdAt = startTime + 450 * oneDay
            )
        )

        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_amundi",
                type = AssetTransactionType.BUY,
                quantity = 300.0,
                pricePerUnit = 110.0,
                date = startTime + 135 * oneDay,
                platformId = "plat_ing",
                createdAt = startTime + 135 * oneDay
            )
        )
        assetTransactions.add(
            AssetTransaction(
                id = "at_${atCounter++}",
                assetId = "asset_amundi",
                type = AssetTransactionType.BUY,
                quantity = 150.0,
                pricePerUnit = 118.0,
                date = startTime + 500 * oneDay,
                platformId = "plat_ing",
                createdAt = startTime + 500 * oneDay
            )
        )

        assetTransactions.forEach { tx ->
            assetTransactionDataSource?.insert(tx)
            // Registrar el precio de cada compra como histórico en la fecha de la transacción
            if (tx.type == AssetTransactionType.BUY) {
                priceHistoryDataSource?.insert(
                    AssetPriceHistory(
                        id         = "price_${tx.id}",
                        assetId    = tx.assetId,
                        price      = tx.pricePerUnit,
                        recordedAt = tx.date
                    )
                )
            }
        }

        var txCounter = 1

        val expenseCategories = listOf(
            "cat_exp_01",
            "cat_exp_02",
            "cat_exp_03",
            "cat_exp_04",
            "cat_exp_05",
            "cat_exp_06",
            "cat_exp_07",
            "cat_exp_08",
            "cat_exp_loan"
        )
        val expenseDescriptions = mapOf(
            "cat_exp_01" to listOf(
                "Mercadona",
                "Carrefour",
                "Alcampo",
                "El Corte Inglés",
                "Lidl",
                "Aldi",
                "Mercado municipal",
                "Dia",
                "Eroski",
                "Consum"
            ),
            "cat_exp_02" to listOf(
                "Gasolina",
                "Metro",
                "Bus",
                "Taxi",
                "Uber",
                "Parking",
                "Peaje",
                "Bolt",
                "Renfe",
                "Bicicleta"
            ),
            "cat_exp_03" to listOf(
                "Luz",
                "Agua",
                "Gas",
                "Internet",
                "Móvil",
                "Alquiler",
                "Comunidad",
                "Reparación",
                "Lavadora",
                "Menaje"
            ),
            "cat_exp_04" to listOf(
                "Farmacia",
                "Médico",
                "Dentista",
                "Optica",
                "Gimnasio",
                "Mutua",
                "Análisis",
                "Psicólogo"
            ),
            "cat_exp_05" to listOf(
                "Cine",
                "Restaurante",
                "Café",
                "Bar",
                "Concierto",
                "Streaming",
                "Videojuegos",
                "Teatro",
                "Museo",
                "Viaje fin de semana"
            ),
            "cat_exp_06" to listOf(
                "Zara",
                "H&M",
                "Decathlon",
                "Nike",
                "Amazon",
                "Shein",
                "Primark",
                "Mango",
                "El Corte Inglés"
            ),
            "cat_exp_07" to listOf(
                "Curso Udemy",
                "Master",
                "Libro",
                "Universidad",
                "Curso online",
                "Certificación",
                "Idiomas",
                "Conferencia"
            ),
            "cat_exp_08" to listOf(
                "Varios",
                "Regalo",
                "Comisiones",
                "Imprevistos",
                "Donación",
                "Suscripción",
                "Peluquería",
                "Tintorería"
            ),
            "cat_exp_loan" to listOf(
                "Cuota hipoteca",
                "Cuota préstamo personal",
                "Cuota préstamo coche",
                "Amortización anticipada"
            )
        )

        val incomeTypes = listOf(
            es.aviferdev.n3to.domain.model.IncomeType.SALARY,
            es.aviferdev.n3to.domain.model.IncomeType.BANK_INTEREST,
            es.aviferdev.n3to.domain.model.IncomeType.DIVIDEND,
            es.aviferdev.n3to.domain.model.IncomeType.BONUS_PRIZE
        )

        for (dayOffset in 0L until totalDays) {
            val date = startTime + dayOffset * oneDay

            val roll = random.nextFloat()
            val numTransactions = when {
                roll < 0.10 -> 0
                roll < 0.45 -> 1
                roll < 0.80 -> 2
                roll < 0.95 -> 3
                else -> 4
            }

            for (i in 0 until numTransactions) {
                val isIncome = random.nextFloat() < 0.15

                if (isIncome) {
                    val incomeType = incomeTypes.random(random)
                    val gross: Double
                    val amount: Double

                    when (incomeType) {
                        es.aviferdev.n3to.domain.model.IncomeType.SALARY -> {
                            gross = 3500.0 + random.nextDouble(-500.0, 500.0)
                            amount = gross * 0.8 - 500
                        }

                        es.aviferdev.n3to.domain.model.IncomeType.DIVIDEND -> {
                            gross = 100.0 + random.nextDouble(0.0, 200.0)
                            amount = gross * 0.81
                        }

                        es.aviferdev.n3to.domain.model.IncomeType.BANK_INTEREST -> {
                            gross = 20.0 + random.nextDouble(0.0, 80.0)
                            amount = gross * 0.81
                        }

                        else -> {
                            gross = 50.0 + random.nextDouble(0.0, 150.0)
                            amount = gross * 0.81
                        }
                    }

                    val tx = Transaction(
                        id = "tx_${txCounter++}",
                        accountId = "acc_main",
                        amount = amount,
                        type = TransactionType.INCOME,
                        categoryId = null,
                        date = date,
                        notes = "Ingreso generado",
                        createdAt = date,
                        incomeType = incomeType,
                        grossAmount = gross
                    )
                    transactionDataSource?.insert(tx.toEntity())

                } else {
                    val categoryId = expenseCategories.random(random)
                    val descriptions = expenseDescriptions[categoryId] ?: listOf("Gasto")
                    val description = descriptions.random(random)
                    val amount = when (categoryId) {
                        "cat_exp_01" -> 30.0 + random.nextDouble(0.0, 150.0)
                        "cat_exp_02" -> 10.0 + random.nextDouble(0.0, 80.0)
                        "cat_exp_03" -> 50.0 + random.nextDouble(0.0, 200.0)
                        "cat_exp_04" -> 15.0 + random.nextDouble(0.0, 150.0)
                        "cat_exp_05" -> 20.0 + random.nextDouble(0.0, 100.0)
                        "cat_exp_06" -> 20.0 + random.nextDouble(0.0, 150.0)
                        "cat_exp_07" -> 15.0 + random.nextDouble(0.0, 200.0)
                        "cat_exp_loan" -> 300.0 + random.nextDouble(0.0, 700.0)
                        else -> 10.0 + random.nextDouble(0.0, 100.0)
                    }

                    val tx = Transaction(
                        id = "tx_${txCounter++}",
                        accountId = "acc_main",
                        amount = amount,
                        type = TransactionType.EXPENSE,
                        categoryId = categoryId,
                        date = date,
                        notes = description,
                        createdAt = date
                    )
                    transactionDataSource?.insert(tx.toEntity())
                }
            }
        }

        listOf(
            Debt(
                id = "debt_01",
                accountId = "acc_main",
                personName = "Carlos",
                amount = 250.0,
                direction = DebtDirection.THEY_OWE,
                date = startTime + 100 * oneDay,
                isPaid = false,
                notes = "Préstamo para viaje",
                createdAt = startTime + 100 * oneDay
            ),
            Debt(
                id = "debt_02",
                accountId = "acc_main",
                personName = "Laura",
                amount = 80.0,
                direction = DebtDirection.I_OWE,
                date = startTime + 200 * oneDay,
                isPaid = false,
                notes = "Cena",
                createdAt = startTime + 200 * oneDay
            ),
            Debt(
                id = "debt_03",
                accountId = "acc_main",
                personName = "Miguel",
                amount = 500.0,
                direction = DebtDirection.THEY_OWE,
                date = startTime + 150 * oneDay,
                isPaid = true,
                notes = "Préstamo coche",
                createdAt = startTime + 150 * oneDay
            ),
            Debt(
                id = "debt_04",
                accountId = "acc_main",
                personName = "Ana",
                amount = 120.0,
                direction = DebtDirection.I_OWE,
                date = startTime + 400 * oneDay,
                isPaid = false,
                notes = "Material oficina",
                createdAt = startTime + 400 * oneDay
            ),
            Debt(
                id = "debt_05",
                accountId = "acc_main",
                personName = "David",
                amount = 350.0,
                direction = DebtDirection.THEY_OWE,
                date = startTime + 80 * oneDay,
                isPaid = false,
                notes = "Préstamo emergencia",
                createdAt = startTime + 80 * oneDay
            )
        ).forEach { debtDataSource?.insert(it.toEntity()) }

        listOf(
            Asset(
                id = "deposit_01",
                accountId = "acc_main",
                ticker = "DEP_BSANTANDER",
                name = "Depósito Banco Santander 4.5% 12M",
                notes = "Depósito a 12 meses",
                createdAt = startTime + 90 * oneDay,
                assetCategoryId = "fixed_cat_fixedincome",
                currentPrice = null,
                maturityDate = startTime + 455 * oneDay
            ),
            Asset(
                id = "deposit_02",
                accountId = "acc_main",
                ticker = "DEP_ING",
                name = "Depósito ING 4.2% 18M",
                notes = "Depósito a 18 meses",
                createdAt = startTime + 250 * oneDay,
                assetCategoryId = "fixed_cat_fixedincome",
                currentPrice = null,
                maturityDate = startTime + 798 * oneDay // 18 meses desde creación en +250d
            ),
            Asset(
                id = "deposit_03",
                accountId = "acc_main",
                ticker = "DEP_CAIXA",
                name = "Depósito CaixaBank 3.8% 6M",
                notes = "Depósito a 6 meses",
                createdAt = startTime + 400 * oneDay,
                assetCategoryId = "fixed_cat_fixedincome",
                currentPrice = null,
                maturityDate = startTime + 580 * oneDay
            )
        ).forEach { assetDataSource?.insert(it) }

        listOf(
            Asset(
                id = "crowd_01",
                accountId = "acc_main",
                ticker = "RAISIN_CL_01",
                name = "Crowdlending Raisin 6.5%",
                notes = "Cartera diversificada Raisin",
                createdAt = startTime + 60 * oneDay,
                assetCategoryId = "fixed_cat_crowdlending",
                currentPrice = null,
                maturityDate = startTime + 425 * oneDay // 12 meses desde creación en +60d
            )
        ).forEach { assetDataSource?.insert(it) }
        assetTransactionDataSource?.insert(
            AssetTransaction(
                id = "at_crowd_01",
                assetId = "crowd_01",
                type = AssetTransactionType.BUY,
                quantity = 1.0,
                pricePerUnit = 5000.0,
                date = startTime + 60 * oneDay,
                platformId = "plat_raisin",
                createdAt = startTime + 60 * oneDay
            )
        )

        listOf(
            Asset(
                id = "bond_01",
                accountId = "acc_main",
                ticker = "ES0000000001",
                name = "Bono Estado Español 2027 3.5%",
                notes = "Bono soberano",
                createdAt = startTime + 180 * oneDay,
                assetCategoryId = "fixed_cat_fixedincome",
                currentPrice = 98.5,
                currentPriceUpdatedAt = endTime,
                maturityDate = startTime + 730 * oneDay // vence ene-2027
            ),
            Asset(
                id = "bond_02",
                accountId = "acc_main",
                ticker = "DE0000000001",
                name = "Bono Alemania 2028 2.5%",
                notes = "Bund",
                createdAt = startTime + 300 * oneDay,
                assetCategoryId = "fixed_cat_fixedincome",
                currentPrice = 101.2,
                currentPriceUpdatedAt = endTime,
                maturityDate = startTime + 1095 * oneDay // vence ene-2028
            )
        ).forEach { asset ->
            assetDataSource?.insert(asset)
            if (asset.currentPrice != null && asset.currentPriceUpdatedAt != null) {
                priceHistoryDataSource?.insert(
                    AssetPriceHistory(
                        id = "price_${asset.id}_init",
                        assetId = asset.id,
                        price = asset.currentPrice,
                        recordedAt = asset.currentPriceUpdatedAt
                    )
                )
            }
        }

        // Letras del Tesoro (renta fija a corto plazo)
        listOf(
            Asset(
                id = "letra_01",
                accountId = "acc_main",
                ticker = "LETRA_6M_25",
                name = "Letra del Tesoro 6M 3.2%",
                notes = "Renta fija soberana a corto plazo",
                createdAt = startTime + 20 * oneDay,
                assetCategoryId = "fixed_cat_fixedincome",
                currentPrice = null,
                maturityDate = startTime + 200 * oneDay
            ),
            Asset(
                id = "letra_02",
                accountId = "acc_main",
                ticker = "LETRA_12M_25",
                name = "Letra del Tesoro 12M 2.8%",
                notes = "Renta fija soberana a corto plazo",
                createdAt = startTime + 20 * oneDay,
                assetCategoryId = "fixed_cat_fixedincome",
                currentPrice = null,
                maturityDate = startTime + 385 * oneDay
            )
        ).forEach { assetDataSource?.insert(it) }

        assetTransactionDataSource?.insert(
            AssetTransaction(
                id = "at_letra_01",
                assetId = "letra_01",
                type = AssetTransactionType.BUY,
                quantity = 1.0,
                pricePerUnit = 9853.0, // nominal 10.000 €, rendimiento ~3.2%
                date = startTime + 20 * oneDay,
                platformId = "plat_singular",
                createdAt = startTime + 20 * oneDay
            )
        )
        assetTransactionDataSource?.insert(
            AssetTransaction(
                id = "at_letra_02",
                assetId = "letra_02",
                type = AssetTransactionType.BUY,
                quantity = 1.0,
                pricePerUnit = 14590.0, // nominal 15.000 €, rendimiento ~2.8%
                date = startTime + 20 * oneDay,
                platformId = "plat_singular",
                createdAt = startTime + 20 * oneDay
            )
        )

        // Hipoteca — iniciada en enero 2023, 25 años, variable Euribor + 0.89%
        val mortgageStart = startTime - 730 * oneDay
        val mortgageEnd   = mortgageStart + 300L * 30 * oneDay // ~25 años

        loanDataSource?.insert(
            Loan(
                id = "loan_mortgage_01",
                accountId = "acc_main",
                name = "Hipoteca vivienda habitual",
                type = LoanType.MORTGAGE,
                totalAmount = 220000.0,
                outstandingPrincipal = 200423.0, // tras 40 cuotas pagadas
                currentInterestRate = 3.2,       // post revisión anual ene-2026
                monthlyPayment = 1070.0,         // recalculada tras bajada de tipo
                totalInstallments = 300,
                paidInstallments = 40,            // ene-2023 → may-2026 ≈ 40 meses
                startDate = mortgageStart,
                endDate = mortgageEnd,
                lenderName = "Banco Santander",
                notes = "Hipoteca variable Euribor + 0.89%",
                archived = false,
                createdAt = mortgageStart
            )
        )

        // Revisión anual del Euribor en enero 2026 → bajada de 3.5% a 3.2%
        loanRateChangeDataSource?.insert(
            LoanRateChange(
                id = "rate_change_01",
                loanId = "loan_mortgage_01",
                newRate = 3.2,
                previousRate = 3.5,
                effectiveDate = startTime + 365 * oneDay,
                createdAt = startTime + 365 * oneDay
            )
        )

        // Vivienda habitual vinculada a la hipoteca
        realEstateDataSource?.insert(
            RealEstateProperty(
                id = "prop_main_home",
                accountId = "acc_main",
                name = "Vivienda habitual",
                address = "Calle Mayor 15, Madrid",
                propertyType = PropertyType.PRIMARY_HOME,
                purchaseValue = 280000.0,
                currentEstimatedValue = 295000.0,
                acquisitionDate = mortgageStart,
                ownershipPercentage = 100.0,
                linkedLoanId = "loan_mortgage_01",
                rentalStatus = RentalStatus.OWN_USE,
                monthlyRent = null,
                mortgageReminderDismissed = false,
                archived = false
            )
        )

        // Fondo de emergencia: 6 meses de gastos estimados a 2.500 €/mes = 15.000 €
        emergencyFundDataSource?.saveEmergencyFund(
            EmergencyFund(
                accountId = "acc_main",
                targetMonths = 6,
                calculationMethod = EmergencyFundMethod.MANUAL,
                manualMonthlyExpense = 2500.0,
                excludedCategoryIds = emptyList()
            )
        )

        // Objetivos mensuales (mes "00" = base del año, "01".."12" = overrides)
        listOf(
            MonthlyGoal(accountId = "acc_main", year = "2025", month = "00", savingsTarget = 500.0,  investmentTarget = 300.0),
            MonthlyGoal(accountId = "acc_main", year = "2025", month = "06", savingsTarget = 200.0,  investmentTarget = 100.0), // julio: vacaciones
            MonthlyGoal(accountId = "acc_main", year = "2025", month = "07", savingsTarget = 150.0,  investmentTarget = 100.0), // agosto: vacaciones
            MonthlyGoal(accountId = "acc_main", year = "2025", month = "12", savingsTarget = 300.0,  investmentTarget = 150.0), // diciembre: navidad
            MonthlyGoal(accountId = "acc_main", year = "2026", month = "00", savingsTarget = 600.0,  investmentTarget = 400.0),
            MonthlyGoal(accountId = "acc_main", year = "2026", month = "01", savingsTarget = 800.0,  investmentTarget = 500.0), // enero: propósitos año nuevo
            MonthlyGoal(accountId = "acc_main", year = "2026", month = "03", savingsTarget = 700.0,  investmentTarget = 400.0), // marzo: sin gastos extras
            MonthlyGoal(accountId = "acc_main", year = "2026", month = "05", savingsTarget = 550.0,  investmentTarget = 350.0)  // mayo: fin del periodo
        ).forEach { goalDataSource?.upsert(it) }
    }

    private fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
        id = id,
        accountId = accountId,
        amount = amount,
        type = type.name,
        categoryId = categoryId,
        date = date,
        notes = notes,
        createdAt = createdAt,
        excludeFromFiscal = if (excludeFromFiscal) 1L else 0L,
        incomeType = incomeType?.name,
        grossAmount = grossAmount,
        commissionAmount = commissionAmount,
        originalCurrency = originalCurrency,
        originalAmount = originalAmount,
        exchangeRate = exchangeRate,
        issuerId = issuerId,
        issuerName = issuerName,
        linkedAssetTransactionId = linkedAssetTransactionId,
        linkedLoanId = linkedLoanId,
        linkedPropertyId = linkedPropertyId,
        linkedValuableId = linkedValuableId
    )

    private fun Debt.toEntity(): DebtEntity = DebtEntity(
        id = id,
        accountId = accountId,
        personName = personName,
        amount = amount,
        direction = direction.name,
        date = date,
        isPaid = if (isPaid) 1L else 0L,
        notes = notes,
        createdAt = createdAt
    )
}
