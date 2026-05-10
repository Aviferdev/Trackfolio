package es.aviferdev.trackfolio.data.database

import es.aviferdev.trackfolio.data.datasource.account.AccountLocalDataSource
import es.aviferdev.trackfolio.data.datasource.asset.AssetCategoryLocalDataSource
import es.aviferdev.trackfolio.data.datasource.asset.AssetLocalDataSource
import es.aviferdev.trackfolio.data.datasource.asset.AssetTransactionLocalDataSource
import es.aviferdev.trackfolio.data.datasource.assetmetadata.AssetMetadataLocalDataSource
import es.aviferdev.trackfolio.data.datasource.debt.DebtLocalDataSource
import es.aviferdev.trackfolio.data.datasource.platform.PlatformLocalDataSource
import es.aviferdev.trackfolio.data.datasource.transaction.TransactionCategoryLocalDataSource
import es.aviferdev.trackfolio.data.datasource.transaction.TransactionLocalDataSource
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.AccountType
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.AssetRegion
import es.aviferdev.trackfolio.domain.model.AssetSector
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random
import kotlin.time.TimeSource

class DatabaseInitializer(
    private val transactionCategoryDataSource: TransactionCategoryLocalDataSource,
    private val assetCategoryDataSource: AssetCategoryLocalDataSource,
    private val assetMetadataDataSource: AssetMetadataLocalDataSource? = null,
    private val accountDataSource: AccountLocalDataSource? = null,
    private val transactionDataSource: TransactionLocalDataSource? = null,
    private val assetDataSource: AssetLocalDataSource? = null,
    private val assetTransactionDataSource: AssetTransactionLocalDataSource? = null,
    private val platformDataSource: PlatformLocalDataSource? = null,
    private val debtDataSource: DebtLocalDataSource? = null
) {
    companion object {

        /** ID fijo de la categoría de ajuste de saldo. Usado por ReconcileBalanceUseCase. */
        const val ADJUSTMENT_CATEGORY_ID = "cat_adj_reconciliation"

        val DEFAULT_ASSET_CATEGORIES = listOf(
            AssetCategory(id = "fixed_cat_stocks",       name = "Acciones",              icon = "📊", sortOrder = 0, createdAt = 0),
            AssetCategory(id = "fixed_cat_etfs",         name = "ETFs",                  icon = "📈", sortOrder = 1, createdAt = 0),
            AssetCategory(id = "fixed_cat_funds",        name = "Fondos de inversión",   icon = "💼", sortOrder = 2, createdAt = 0),
            AssetCategory(id = "fixed_cat_crypto",       name = "Criptomonedas",         icon = "₿",  sortOrder = 3, createdAt = 0),
            AssetCategory(id = "fixed_cat_pensions",     name = "Planes de pensiones",   icon = "🛡", sortOrder = 4, createdAt = 0),
            AssetCategory(id = "fixed_cat_fixedincome",  name = "Renta fija",            icon = "🏦", sortOrder = 5, createdAt = 0),
            AssetCategory(id = "fixed_cat_commodities",  name = "Materias primas",       icon = "🪙", sortOrder = 7, createdAt = 0),
            AssetCategory(id = "fixed_cat_crowdlending", name = "Crowdlending",          icon = "🤝", sortOrder = 8, createdAt = 0),
        )

        val DEFAULT_ASSET_SECTORS = listOf(
            AssetSector(id = "sector_tech", name = "Tecnología", icon = "💻", createdAt = 0),
            AssetSector(id = "sector_health", name = "Salud", icon = "🏥", createdAt = 0),
            AssetSector(id = "sector_energy", name = "Energía", icon = "⚡", createdAt = 0),
            AssetSector(id = "sector_finance", name = "Financiero", icon = "🏦", createdAt = 0),
            AssetSector(id = "sector_consumer", name = "Consumo", icon = "🛒", createdAt = 0),
            AssetSector(id = "sector_industrial", name = "Industrial", icon = "🏭", createdAt = 0),
            AssetSector(id = "sector_realestate", name = "Inmobiliario", icon = "🏠", createdAt = 0),
            AssetSector(id = "sector_telecom", name = "Telecomunicaciones", icon = "📡", createdAt = 0),
            AssetSector(id = "sector_materials", name = "Materias Primas", icon = "🪙", createdAt = 0),
            AssetSector(id = "sector_utilities", name = "Servicios Públicos", icon = "💡", createdAt = 0),
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

        val DEFAULT_EXPENSE_CATEGORIES = listOf(
            CategoryEntity(id = "cat_exp_01", name = "Alimentación", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_02", name = "Transporte",   type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_03", name = "Hogar",        type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_04", name = "Salud",        type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_05", name = "Ocio",         type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_06", name = "Ropa",         type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_07", name = "Educación",    type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_08", name = "Otros",        type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
            CategoryEntity(id = "cat_exp_loan", name = "Cuota préstamo", type = TransactionType.EXPENSE.name, isDefault = 1L, archived = 0L),
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
        // 1 de enero de 2026 00:00:00 UTC
        val currentTime = 1767225600000L
        val oneDay = 24 * 60 * 60 * 1000L
        val oneYear = 365L * oneDay

        // ==================== PLATAFORMAS ====================
        val platforms = listOf(
            Platform(id = "plat_ibkr", name = "Interactive Brokers", icon = "🟢", sortOrder = 0, createdAt = currentTime),
            Platform(id = "plat_singular", name = "Singular Bank", icon = "🏦", sortOrder = 1, createdAt = currentTime),
            Platform(id = "plat_ing", name = "ING", icon = "🟠", sortOrder = 2, createdAt = currentTime),
            Platform(id = "plat_binance", name = "Binance", icon = "🟡", sortOrder = 3, createdAt = currentTime),
            Platform(id = "plat_raisin", name = "Raisin", icon = "🌾", sortOrder = 4, createdAt = currentTime)
        )
        platforms.forEach { platformDataSource?.insert(it) }

        // ==================== CUENTA ÚNICA ====================
        val mainAccount = Account(
            id = "acc_main",
            name = "Cuenta Principal",
            currency = "EUR",
            initialBalance = 50000.0,
            computedBalance = 50000.0,
            createdAt = currentTime,
            accountType = AccountType.GENERAL
        )
        accountDataSource?.insertAccount(mainAccount)

        // ==================== ACTIVOS (8 activos) ====================
        val assets = listOf(
            Asset(id = "asset_aapl", accountId = "acc_main", ticker = "AAPL", name = "Apple Inc.", notes = "Tech", createdAt = currentTime + 10 * oneDay, assetCategoryId = "fixed_cat_stocks", currentPrice = 185.50, currentPriceUpdatedAt = currentTime + 350 * oneDay),
            Asset(id = "asset_msft", accountId = "acc_main", ticker = "MSFT", name = "Microsoft Corporation", notes = "Tech", createdAt = currentTime + 15 * oneDay, assetCategoryId = "fixed_cat_stocks", currentPrice = 415.20, currentPriceUpdatedAt = currentTime + 350 * oneDay),
            Asset(id = "asset_nvda", accountId = "acc_main", ticker = "NVDA", name = "NVIDIA Corporation", notes = "Tech - Semiconductors", createdAt = currentTime + 20 * oneDay, assetCategoryId = "fixed_cat_stocks", currentPrice = 875.30, currentPriceUpdatedAt = currentTime + 350 * oneDay),
            Asset(id = "asset_vo", accountId = "acc_main", ticker = "VO", name = "Vanguard Consumer Staples ETF", notes = "ETF", createdAt = currentTime + 25 * oneDay, assetCategoryId = "fixed_cat_etfs", currentPrice = 245.80, currentPriceUpdatedAt = currentTime + 350 * oneDay),
            Asset(id = "asset_ieac", accountId = "acc_main", ticker = "IEAC", name = "iShares MSCI Europe UCITS", notes = "ETF Europa", createdAt = currentTime + 30 * oneDay, assetCategoryId = "fixed_cat_etfs", currentPrice = 158.40, currentPriceUpdatedAt = currentTime + 350 * oneDay),
            Asset(id = "asset_btc", accountId = "acc_main", ticker = "BTC", name = "Bitcoin", notes = "Crypto", createdAt = currentTime + 35 * oneDay, assetCategoryId = "fixed_cat_crypto", currentPrice = 67500.0, currentPriceUpdatedAt = currentTime + 350 * oneDay),
            Asset(id = "asset_eth", accountId = "acc_main", ticker = "ETH", name = "Ethereum", notes = "Crypto", createdAt = currentTime + 40 * oneDay, assetCategoryId = "fixed_cat_crypto", currentPrice = 3450.0, currentPriceUpdatedAt = currentTime + 350 * oneDay),
            Asset(id = "asset_amundi", accountId = "acc_main", ticker = "AMUNDI", name = "Amundi Europa Stoxx 600", notes = "Fondo indexado", createdAt = currentTime + 45 * oneDay, assetCategoryId = "fixed_cat_funds", currentPrice = 125.40, currentPriceUpdatedAt = currentTime + 350 * oneDay)
        )
        assets.forEach { assetDataSource?.insert(it) }

        // ==================== TRANSACCIONES DE ACTIVOS (~20 operaciones) ====================
        val assetTransactions = mutableListOf<AssetTransaction>()
        var atCounter = 1

        // Apple - varias compras
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_aapl", type = AssetTransactionType.BUY, quantity = 50.0, pricePerUnit = 145.0, date = currentTime + 10 * oneDay, platformId = "plat_ibkr", createdAt = currentTime + 10 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_aapl", type = AssetTransactionType.BUY, quantity = 30.0, pricePerUnit = 165.0, date = currentTime + 80 * oneDay, platformId = "plat_ibkr", createdAt = currentTime + 80 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_aapl", type = AssetTransactionType.BUY, quantity = 20.0, pricePerUnit = 180.0, date = currentTime + 200 * oneDay, platformId = "plat_ibkr", createdAt = currentTime + 200 * oneDay))

        // Microsoft
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_msft", type = AssetTransactionType.BUY, quantity = 40.0, pricePerUnit = 350.0, date = currentTime + 15 * oneDay, platformId = "plat_ibkr", createdAt = currentTime + 15 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_msft", type = AssetTransactionType.BUY, quantity = 25.0, pricePerUnit = 400.0, date = currentTime + 150 * oneDay, platformId = "plat_ibkr", createdAt = currentTime + 150 * oneDay))

        // NVIDIA
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_nvda", type = AssetTransactionType.BUY, quantity = 15.0, pricePerUnit = 450.0, date = currentTime + 20 * oneDay, platformId = "plat_ibkr", createdAt = currentTime + 20 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_nvda", type = AssetTransactionType.BUY, quantity = 10.0, pricePerUnit = 650.0, date = currentTime + 180 * oneDay, platformId = "plat_ibkr", createdAt = currentTime + 180 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_nvda", type = AssetTransactionType.SELL, quantity = 5.0, pricePerUnit = 850.0, date = currentTime + 300 * oneDay, platformId = "plat_ibkr", createdAt = currentTime + 300 * oneDay))

        // ETFs
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_vo", type = AssetTransactionType.BUY, quantity = 100.0, pricePerUnit = 220.0, date = currentTime + 25 * oneDay, platformId = "plat_singular", createdAt = currentTime + 25 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_ieac", type = AssetTransactionType.BUY, quantity = 200.0, pricePerUnit = 145.0, date = currentTime + 30 * oneDay, platformId = "plat_singular", createdAt = currentTime + 30 * oneDay))

        // Crypto
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_btc", type = AssetTransactionType.BUY, quantity = 0.5, pricePerUnit = 42000.0, date = currentTime + 35 * oneDay, platformId = "plat_binance", createdAt = currentTime + 35 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_btc", type = AssetTransactionType.BUY, quantity = 0.25, pricePerUnit = 55000.0, date = currentTime + 180 * oneDay, platformId = "plat_binance", createdAt = currentTime + 180 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_eth", type = AssetTransactionType.BUY, quantity = 8.0, pricePerUnit = 2800.0, date = currentTime + 40 * oneDay, platformId = "plat_binance", createdAt = currentTime + 40 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_eth", type = AssetTransactionType.BUY, quantity = 3.0, pricePerUnit = 3200.0, date = currentTime + 210 * oneDay, platformId = "plat_binance", createdAt = currentTime + 210 * oneDay))

        // Fondo
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_amundi", type = AssetTransactionType.BUY, quantity = 300.0, pricePerUnit = 110.0, date = currentTime + 45 * oneDay, platformId = "plat_ing", createdAt = currentTime + 45 * oneDay))
        assetTransactions.add(AssetTransaction(id = "at_${atCounter++}", assetId = "asset_amundi", type = AssetTransactionType.BUY, quantity = 150.0, pricePerUnit = 118.0, date = currentTime + 250 * oneDay, platformId = "plat_ing", createdAt = currentTime + 250 * oneDay))

        assetTransactions.forEach { assetTransactionDataSource?.insert(it) }

        // ==================== ~700 TRANSACCIONES EN 2026 ====================
        var txCounter = 1

        val expenseCategories = listOf("cat_exp_01", "cat_exp_02", "cat_exp_03", "cat_exp_04", "cat_exp_05", "cat_exp_06", "cat_exp_07", "cat_exp_08")
        val expenseDescriptions = mapOf(
            "cat_exp_01" to listOf("Mercadona", "Carrefour", "Alcampo", "El Corte Inglés", "Lidl", "Aldi", "Mercado municipal", "Dia", "Eroski", "Consum"),
            "cat_exp_02" to listOf("Gasolina", "Metro", "Bus", "Taxi", "Uber", "Parking", "Peaje", "Bolt", "Renfe", "Bicicleta"),
            "cat_exp_03" to listOf("Luz", "Agua", "Gas", "Internet", "Móvil", "Alquiler", "Comunidad", "Reparación", "Lavadora", "Menaje"),
            "cat_exp_04" to listOf("Farmacia", "Médico", "Dentista", "Optica", "Gimnasio", "Mutua", "Análisis", "Psicólogo"),
            "cat_exp_05" to listOf("Cine", "Restaurante", "Café", "Bar", "Concierto", "Streaming", "Videojuegos", "Teatro", "Museo", "Viaje fin de semana"),
            "cat_exp_06" to listOf("Zara", "H&M", "Decathlon", "Nike", "Amazon", "Shein", "Primark", "Mango", "El Corte Inglés"),
            "cat_exp_07" to listOf("Curso Udemy", "Master", "Libro", "Universidad", "Curso online", "Certificación", "Idiomas", "Conferencia"),
            "cat_exp_08" to listOf("Varios", "Regalo", "Comisiones", "Imprevistos", "Donación", "Suscripción", "Peluquería", "Tintorería")
        )

        val incomeTypes = listOf(
            es.aviferdev.trackfolio.domain.model.IncomeType.SALARY,
            es.aviferdev.trackfolio.domain.model.IncomeType.BANK_INTEREST,
            es.aviferdev.trackfolio.domain.model.IncomeType.DIVIDEND,
            es.aviferdev.trackfolio.domain.model.IncomeType.BONUS_PRIZE
        )

        // Generar transacciones durante todo 2026 (365 días)
        // Objetivo: ~700 transacciones → ~1.92 por día en promedio
        for (dayOffset in 0L until 365L) {
            val date = currentTime + dayOffset * oneDay

            // Distribución: 10% sin tx, 35% con 1, 35% con 2, 15% con 3, 5% con 4
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
                    val amount: Double
                    val gross: Double
                    val irpf: Double
                    val ss: Double?

                    when (incomeType) {
                        es.aviferdev.trackfolio.domain.model.IncomeType.SALARY -> {
                            gross = 3500.0 + random.nextDouble(-500.0, 500.0)
                            amount = gross * 0.8 - 500
                            irpf = 20.0
                            ss = 500.0
                        }
                        es.aviferdev.trackfolio.domain.model.IncomeType.DIVIDEND -> {
                            gross = 100.0 + random.nextDouble(0.0, 200.0)
                            amount = gross * 0.81
                            irpf = 19.0
                            ss = null
                        }
                        es.aviferdev.trackfolio.domain.model.IncomeType.BANK_INTEREST -> {
                            gross = 20.0 + random.nextDouble(0.0, 80.0)
                            amount = gross * 0.81
                            irpf = 19.0
                            ss = null
                        }
                        else -> {
                            gross = 50.0 + random.nextDouble(0.0, 150.0)
                            amount = gross * 0.81
                            irpf = 19.0
                            ss = null
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
                        grossAmount = gross,
                        irpfPercent = irpf,
                        socialSecurityAmount = ss
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

        // ==================== DEUDAS ====================
        val debts = listOf(
            Debt(id = "debt_01", accountId = "acc_main", personName = "Carlos", amount = 250.0, direction = DebtDirection.THEY_OWE, date = currentTime + 45 * oneDay, isPaid = false, notes = "Préstamo para viaje", createdAt = currentTime + 45 * oneDay),
            Debt(id = "debt_02", accountId = "acc_main", personName = "Laura", amount = 80.0, direction = DebtDirection.I_OWE, date = currentTime + 90 * oneDay, isPaid = false, notes = "Cena", createdAt = currentTime + 90 * oneDay),
            Debt(id = "debt_03", accountId = "acc_main", personName = "Miguel", amount = 500.0, direction = DebtDirection.THEY_OWE, date = currentTime + 60 * oneDay, isPaid = true, notes = "Préstamo coche", createdAt = currentTime + 60 * oneDay),
            Debt(id = "debt_04", accountId = "acc_main", personName = "Ana", amount = 120.0, direction = DebtDirection.I_OWE, date = currentTime + 180 * oneDay, isPaid = false, notes = "Material oficina", createdAt = currentTime + 180 * oneDay),
            Debt(id = "debt_05", accountId = "acc_main", personName = "David", amount = 350.0, direction = DebtDirection.THEY_OWE, date = currentTime + 30 * oneDay, isPaid = false, notes = "Préstamo emergencia", createdAt = currentTime + 30 * oneDay)
        )
        debts.forEach { debtDataSource?.insert(it.toEntity()) }

        // ==================== DEPÓSITOS BANCARIOS (Renta Fija) ====================
        val deposits = listOf(
            Asset(id = "deposit_01", accountId = "acc_main", ticker = "DEP_BSANTANDER", name = "Depósito Banco Santander 4.5% 12M", notes = "Depósito a 12 meses", createdAt = currentTime + 30 * oneDay, assetCategoryId = "fixed_cat_fixedincome", currentPrice = null, maturityDate = currentTime + 395 * oneDay),
            Asset(id = "deposit_02", accountId = "acc_main", ticker = "DEP_ING", name = "Depósito ING 4.2% 18M", notes = "Depósito a 18 meses", createdAt = currentTime + 90 * oneDay, assetCategoryId = "fixed_cat_fixedincome", currentPrice = null, maturityDate = currentTime + 640 * oneDay),
            Asset(id = "deposit_03", accountId = "acc_main", ticker = "DEP_CAIXA", name = "Depósito CaixaBank 3.8% 6M", notes = "Depósito a 6 meses", createdAt = currentTime + 150 * oneDay, assetCategoryId = "fixed_cat_fixedincome", currentPrice = null, maturityDate = currentTime + 332 * oneDay)
        )
        deposits.forEach { assetDataSource?.insert(it) }

        // ==================== BONOS / RENTA FIJA ====================
        val bonds = listOf(
            Asset(id = "bond_01", accountId = "acc_main", ticker = "ES0000000001", name = "Bono Estado Español 2027 3.5%", notes = "Bono soberano", createdAt = currentTime + 60 * oneDay, assetCategoryId = "fixed_cat_fixedincome", currentPrice = 98.5, currentPriceUpdatedAt = currentTime + 350 * oneDay, maturityDate = currentTime + 730 * oneDay),
            Asset(id = "bond_02", accountId = "acc_main", ticker = "DE0000000001", name = "Bono Alemania 2028 2.5%", notes = "Bund", createdAt = currentTime + 120 * oneDay, assetCategoryId = "fixed_cat_fixedincome", currentPrice = 101.2, currentPriceUpdatedAt = currentTime + 350 * oneDay, maturityDate = currentTime + 1095 * oneDay)
        )
        bonds.forEach { assetDataSource?.insert(it) }
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
        irpfPercent = irpfPercent,
        socialSecurityAmount = socialSecurityAmount,
        commissionAmount = commissionAmount,
        issuerId = issuerId,
        issuerName = issuerName,
        linkedAssetTransactionId = linkedAssetTransactionId,
        linkedLoanId = linkedLoanId
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
