package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import es.aviferdev.trackfolio.domain.model.FixedIncomeSummary
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.repository.AssetPlatformRepository
import es.aviferdev.trackfolio.domain.portfolio.AssetPosition
import es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator
import es.aviferdev.trackfolio.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.ArchiveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.GetTransactionsByAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.SaveAssetTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.SyncAssetTransactionToLedgerUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.CreateFixedIncomePositionUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.GetFixedIncomeSummaryUseCase
import es.aviferdev.trackfolio.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import es.aviferdev.trackfolio.ui.theme.CategoryPalette
import es.aviferdev.trackfolio.ui.theme.UncategorizedColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

// ─── Estado de cada activo enriquecido con su posición FIFO ──────────────────
data class AssetRow(
    val asset: Asset,
    val position: AssetPosition
)

/** Grupo de activos abiertos pertenecientes a la misma categoría (o "Sin categoría"). */
data class CategoryGroup(
    val category: AssetCategory?,
    val rows: List<AssetRow>,
    val totalInvested: Double,           // coste de las unidades aún en cartera
    val totalCurrentValue: Double,       // valor actual de mercado
    val totalUnrealizedPnL: Double,      // P&L latente
    val totalRealizedPnL: Double,        // P&L cerrado por ventas (para mostrar)
    val totalPnL: Double,                // realized + unrealized
    val totalPnLPercent: Double          // sobre el invertido remanente
) {
    val displayName: String   get() = category?.name ?: "Sin categoría"
    val displayIcon: String   get() = category?.icon ?: "❔"
    val sortKey: Int          get() = category?.sortOrder ?: Int.MAX_VALUE
}

data class CategorySlice(
    val categoryId: String?,
    val name: String,
    val icon: String,
    val value: Double,
    val percent: Double,
    val color: Color
)

data class PortfolioUiState(
    val groups: List<CategoryGroup>     = emptyList(),         // posiciones abiertas
    val closedPositions: List<AssetRow> = emptyList(),         // qty==0 y al menos una venta registrada
    val distribution: List<CategorySlice> = emptyList(),
    val totalInvested: Double           = 0.0,
    val totalCurrentValue: Double       = 0.0,
    val totalRealizedPnL: Double        = 0.0,
    val totalUnrealizedPnL: Double      = 0.0,
    val totalPnL: Double                = 0.0,
    val totalPnLPercent: Double         = 0.0,
    val openPositionsCount: Int         = 0,
    val currencyCode: String            = "EUR",
    val fixedIncomeSummary: FixedIncomeSummary? = null,
    val nearMaturityPositions: List<es.aviferdev.trackfolio.domain.model.FixedIncomePosition> = emptyList(),
    val combinedInvested: Double         = 0.0,
    val combinedCurrentValue: Double     = 0.0,
    val combinedPnL: Double             = 0.0,
    val combinedPnLPercent: Double       = 0.0,
    val combinedRealizedPnL: Double      = 0.0,
    val combinedUnrealizedPnL: Double    = 0.0,
    /** Activos del catálogo (incluso sin movimientos) — para el selector. */
    val allAssets: List<Asset>          = emptyList(),
    val platforms: List<Platform>       = emptyList(),
    val platformsByAsset: Map<String, List<Platform>> = emptyMap(),
    val isLoading: Boolean              = true,
    val error: String?                  = null,

    // Sheet rápido de actualización de precio (mantenido del flujo anterior)
    val showUpdatePriceSheet: Boolean   = false,
    val pricingAsset: Asset?            = null,

    // Sheet de "Nuevo movimiento" desde el FAB del Portfolio (solo activos no renta fija)
    val showAddTxSheet: Boolean         = false,
    // Sheet de dividendo desde el FAB del Portfolio
    val showDividendSheet: Boolean      = false,
    val dividendAssetId: String?        = null,

    // Sheet de nueva posición de renta fija
    val showCreateFixedIncomeSheet: Boolean = false,
    val currentAccountId: String?           = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModel(
    private val getAssetsByAccount: GetAssetsByAccountUseCase,
    private val saveAsset: SaveAssetUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase,
    private val archiveAsset: ArchiveAssetUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val getAccountById: GetAccountByIdUseCase,
    private val getTransactionsByAccount: GetTransactionsByAccountUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val saveAssetTransaction: SaveAssetTransactionUseCase,
    private val syncToLedger: SyncAssetTransactionToLedgerUseCase,
    private val assetPlatformRepository: AssetPlatformRepository,
    private val session: AccountSession,
    private val getFixedIncomeSummary: GetFixedIncomeSummaryUseCase? = null,
    private val getNearMaturityPositions: es.aviferdev.trackfolio.domain.usecase.fixedincome.GetNearMaturityPositionsUseCase? = null,
    private val createFixedIncomePosition: CreateFixedIncomePositionUseCase? = null
) : ViewModel() {

    private val _sheetState = MutableStateFlow(SheetState())

    private data class SheetState(
        val showUpdatePriceSheet: Boolean = false,
        val pricingAsset: Asset? = null,
        val showAddTxSheet: Boolean = false,
        val showDividendSheet: Boolean = false,
        val dividendAssetId: String? = null,
        val showCreateFixedIncomeSheet: Boolean = false,
        val error: String? = null
    )

    private data class BasicPortfolioData(
        val assets: List<Asset>,
        val categories: List<AssetCategory>,
        val account: Account?,
        val transactions: List<AssetTransaction>,
        val platforms: List<Platform>
    )

    private data class BasicPortfolioDataWithFI(
        val assets: List<Asset>,
        val categories: List<AssetCategory>,
        val account: Account?,
        val transactions: List<AssetTransaction>,
        val platforms: List<Platform>,
        val fiSummary: FixedIncomeSummary?,
        val nearMaturityPositions: List<es.aviferdev.trackfolio.domain.model.FixedIncomePosition>
    )

    val portfolioState: StateFlow<PortfolioUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) {
                flowOf(PortfolioUiState(isLoading = false))
            } else {
                val fiFlow = if (getFixedIncomeSummary != null) {
                    getFixedIncomeSummary(accountId)
                } else flowOf(null)

                val nearMaturityFlow = if (getNearMaturityPositions != null) {
                    getNearMaturityPositions(accountId)
                } else flowOf(emptyList())

                val baseDataFlow = combine(
                    getAssetsByAccount(accountId),
                    getAssetCategoriesIncludingArchived(),
                    getAccountById(accountId),
                    getTransactionsByAccount(accountId),
                    getPlatforms()
                ) { assets, categories, account, txs, platforms ->
                    BasicPortfolioData(assets, categories, account, txs, platforms)
                }

                combine(baseDataFlow, fiFlow, nearMaturityFlow) { baseData, fiSummary, nearMaturity ->
                    BasicPortfolioDataWithFI(
                        assets = baseData.assets,
                        categories = baseData.categories,
                        account = baseData.account,
                        transactions = baseData.transactions,
                        platforms = baseData.platforms,
                        fiSummary = fiSummary,
                        nearMaturityPositions = nearMaturity
                    )
                }.flatMapLatest { basicData ->
                    val assetIds = basicData.assets.map { it.id }
                    if (assetIds.isEmpty()) {
                        flowOf(buildState(
                            assets = basicData.assets,
                            categories = basicData.categories,
                            account = basicData.account,
                            transactions = basicData.transactions,
                            platforms = basicData.platforms,
                            platformsByAsset = emptyMap(),
                            fiSummary = basicData.fiSummary,
                            nearMaturityPositions = basicData.nearMaturityPositions,
                            accountId = basicData.account?.id
                        ))
                    } else {
                        assetPlatformRepository.getPlatformsByAssets(assetIds).map { platformsByAsset ->
                            buildState(
                                assets = basicData.assets,
                                categories = basicData.categories,
                                account = basicData.account,
                                transactions = basicData.transactions,
                                platforms = basicData.platforms,
                                platformsByAsset = platformsByAsset,
                                fiSummary = basicData.fiSummary,
                                nearMaturityPositions = basicData.nearMaturityPositions,
                                accountId = basicData.account?.id
                            )
                        }
                    }
                }
            }
        }
        .combine(_sheetState) { state, sheets ->
            state.copy(
                showUpdatePriceSheet        = sheets.showUpdatePriceSheet,
                pricingAsset                = sheets.pricingAsset,
                showAddTxSheet              = sheets.showAddTxSheet,
                showDividendSheet           = sheets.showDividendSheet,
                dividendAssetId             = sheets.dividendAssetId,
                showCreateFixedIncomeSheet  = sheets.showCreateFixedIncomeSheet,
                error                       = sheets.error
            )
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = PortfolioUiState()
        )

    val availableCategories: StateFlow<List<AssetCategory>> = getAssetCategoriesIncludingArchived()
        .map { list -> list.filter { !it.archived } }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private fun buildState(
        assets: List<Asset>,
        categories: List<AssetCategory>,
        account: Account?,
        transactions: List<AssetTransaction>,
        platforms: List<Platform>,
        platformsByAsset: Map<String, List<Platform>>,
        fiSummary: FixedIncomeSummary?,
        nearMaturityPositions: List<es.aviferdev.trackfolio.domain.model.FixedIncomePosition>,
        accountId: String? = null
    ): PortfolioUiState {
        // Agrupar movimientos por activo.
        val txByAsset: Map<String, List<AssetTransaction>> =
            transactions.groupBy { it.assetId }

        // Para cada activo, calcular su posición FIFO y separar:
        //  - openRows  → tienen qty > 0 (posiciones vivas)
        //  - closedRows → qty == 0 y se ha registrado al menos una venta
        //                 (incluye P&L = 0; el usuario debe poder revisar
        //                  la operación aunque haya cerrado a tablas)
        //  - silent    → qty == 0 y nunca hubo ventas (catálogo en blanco
        //                 o solo compras sin cerrar — no aplica aquí)
        val openRows   = mutableListOf<AssetRow>()
        val closedRows = mutableListOf<AssetRow>()
        for (asset in assets) {
            val txs = txByAsset[asset.id].orEmpty()
            val pos = PortfolioCalculator.calculate(txs, asset.currentPrice)
            val hasSales = txs.any { it.type == AssetTransactionType.SELL }
            when {
                pos.netQuantity > 0.0  -> openRows.add(AssetRow(asset, pos))
                hasSales               -> closedRows.add(AssetRow(asset, pos))
                else                   -> { /* silenciado: catálogo sin ventas */ }
            }
        }

        val byId = categories.associateBy { it.id }
        val grouped: Map<String?, List<AssetRow>> =
            openRows.groupBy { it.asset.assetCategoryId }

        val groups: List<CategoryGroup> = grouped
            .map { (categoryId, groupRows) ->
                val cat = categoryId?.let { byId[it] }
                val invested  = groupRows.sumOf { it.position.totalInvestedRemaining }
                val current   = groupRows.sumOf { it.position.currentValue }
                val realized  = groupRows.sumOf { it.position.realizedPnL }
                val unrealized = groupRows.sumOf { it.position.unrealizedPnL }
                val total     = realized + unrealized
                CategoryGroup(
                    category           = cat,
                    rows               = groupRows.sortedByDescending { it.position.currentValue },
                    totalInvested      = invested,
                    totalCurrentValue  = current,
                    totalUnrealizedPnL = unrealized,
                    totalRealizedPnL   = realized,
                    totalPnL           = total,
                    totalPnLPercent    = if (invested > 0.0) (total / invested) * 100.0 else 0.0
                )
            }
            .sortedWith(
                compareBy(
                    { if (it.category == null) 1 else 0 },
                    { it.sortKey },
                    { it.displayName }
                )
            )

        val totalInvested      = groups.sumOf { it.totalInvested }
        val totalCurrentValue  = groups.sumOf { it.totalCurrentValue }
        // El P&L realizado SUMA tanto el de las posiciones abiertas (ventas
        // parciales) como el de las cerradas (vendidas por completo).
        val totalRealizedPnL   = groups.sumOf { it.totalRealizedPnL } +
                                 closedRows.sumOf { it.position.realizedPnL }
        val totalUnrealizedPnL = groups.sumOf { it.totalUnrealizedPnL }
        val totalPnL           = totalRealizedPnL + totalUnrealizedPnL

        val fiTotalPrincipal    = fiSummary?.totalPrincipal ?: 0.0
        val fiTotalCurrentValue = fiSummary?.totalCurrentValue ?: 0.0
        val fiTotalNetProfit    = fiSummary?.totalNetProfit ?: 0.0

        val combinedInvested     = totalInvested + fiTotalPrincipal
        val combinedCurrentValue = totalCurrentValue + fiTotalCurrentValue
        val combinedPnL          = totalPnL + fiTotalNetProfit
        val combinedPnLPercent    = if (combinedInvested > 0.0) (combinedPnL / combinedInvested) * 100.0 else 0.0

        val distribution: List<CategorySlice> = if (combinedCurrentValue <= 0.0) {
            emptyList()
        } else {
            val slices = mutableListOf<CategorySlice>()

            if (fiTotalCurrentValue > 0.0) {
                slices.add(
                    CategorySlice(
                        categoryId = "fixed_income",
                        name       = "Renta fija",
                        icon       = "🏦",
                        value      = fiTotalCurrentValue,
                        percent    = (fiTotalCurrentValue / combinedCurrentValue) * 100.0,
                        color      = Color(0xFF4CAF50)
                    )
                )
            }

            groups
                .filter { it.totalCurrentValue > 0.0 }
                .mapIndexed { idx, g ->
                    CategorySlice(
                        categoryId = g.category?.id,
                        name       = g.displayName,
                        icon       = g.displayIcon,
                        value      = g.totalCurrentValue,
                        percent    = (g.totalCurrentValue / combinedCurrentValue) * 100.0,
                        color      = colorForGroup(g, idx)
                    )
                }
                .let { slices.addAll(it) }

            slices.sortedByDescending { it.percent }
        }

        return PortfolioUiState(
            groups             = groups,
            closedPositions    = closedRows.sortedByDescending { it.position.realizedPnL },
            distribution       = distribution,
            totalInvested      = totalInvested,
            totalCurrentValue  = totalCurrentValue,
            totalRealizedPnL   = totalRealizedPnL,
            totalUnrealizedPnL = totalUnrealizedPnL,
            totalPnL           = totalPnL,
            totalPnLPercent    = if (totalInvested > 0.0) (totalPnL / totalInvested) * 100.0 else 0.0,
            openPositionsCount = openRows.size,
            currencyCode       = account?.currency ?: "EUR",
            allAssets          = assets,
            platforms          = platforms,
            platformsByAsset  = platformsByAsset,
            isLoading          = false,
            fixedIncomeSummary = fiSummary,
            nearMaturityPositions = nearMaturityPositions,
            combinedInvested   = combinedInvested,
            combinedCurrentValue = combinedCurrentValue,
            combinedPnL        = combinedPnL,
            combinedPnLPercent = combinedPnLPercent,
            combinedRealizedPnL = totalRealizedPnL + (fiSummary?.totalCollectedInterest ?: 0.0),
            combinedUnrealizedPnL = totalUnrealizedPnL + (fiSummary?.totalAccruedInterest ?: 0.0),
            currentAccountId = accountId
        )
    }

    private fun colorForGroup(group: CategoryGroup, fallbackIndex: Int): Color {
        val cat = group.category ?: return UncategorizedColor
        val idx = if (cat.sortOrder >= 0) cat.sortOrder else fallbackIndex
        return CategoryPalette[idx % CategoryPalette.size]
    }

    // ── Sheet rápido de actualización de precio ──────────────────────────────
    fun openUpdatePriceSheet(asset: Asset) {
        _sheetState.value = _sheetState.value.copy(showUpdatePriceSheet = true, pricingAsset = asset)
    }

    fun closeUpdatePriceSheet() {
        _sheetState.value = _sheetState.value.copy(showUpdatePriceSheet = false, pricingAsset = null)
    }

    fun refreshCurrentPrice(asset: Asset, newPrice: Double) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            updateAssetCurrentPrice(asset.id, newPrice, now)
                .onSuccess { closeUpdatePriceSheet() }
                .onFailure { _sheetState.value = _sheetState.value.copy(error = it.message) }
        }
    }

    // ── Sheet de "Nuevo movimiento" desde el FAB ────────────────────────────
    fun openAddTransactionSheet() {
        _sheetState.value = _sheetState.value.copy(showAddTxSheet = true)
    }

    fun closeAddTransactionSheet() {
        _sheetState.value = _sheetState.value.copy(showAddTxSheet = false)
    }

    fun addTransaction(
        assetId: String,
        type: AssetTransactionType,
        quantity: Double,
        pricePerUnit: Double,
        date: Long,
        platformId: String,
        feeNote: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val tx = AssetTransaction(
                id           = "tx_${now}_${(0..9999).random()}",
                assetId      = assetId,
                type         = type,
                quantity     = quantity,
                pricePerUnit = pricePerUnit,
                date         = date,
                platformId   = platformId,
                feeNote      = feeNote?.ifBlank { null },
                notes        = notes?.ifBlank { null },
                createdAt    = now
            )
            saveAssetTransaction(tx)
                .onSuccess {
                    val asset = portfolioState.value.allAssets.find { it.id == assetId }
                    if (asset != null) {
                        syncToLedger.sync(
                            assetTx   = tx,
                            accountId = asset.accountId,
                            assetName = asset.name
                        )
                        if (type == AssetTransactionType.BUY && isSameDay(date, now)) {
                            updateAssetCurrentPrice(assetId, pricePerUnit, now)
                        }
                    }
                    closeAddTransactionSheet()
                }
                .onFailure { _sheetState.value = _sheetState.value.copy(error = it.message) }
        }
    }

    fun clearError() {
        _sheetState.value = _sheetState.value.copy(error = null)
    }

    // ── Dividendos ─────────────────────────────────────────────────────
    fun openDividendSheet() {
        _sheetState.value = _sheetState.value.copy(showDividendSheet = true, dividendAssetId = null)
    }

    fun closeDividendSheet() {
        _sheetState.value = _sheetState.value.copy(showDividendSheet = false, dividendAssetId = null)
    }

    fun saveDividend(
        assetId: String,
        grossAmount: Double,
        irpfPercent: Double,
        date: Long
    ) {
        viewModelScope.launch {
            val asset = portfolioState.value.allAssets.find { it.id == assetId }
            if (asset == null) {
                _sheetState.value = _sheetState.value.copy(error = "Activo no encontrado")
                return@launch
            }
            val dividendId = "div_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
            val result = syncToLedger.syncDividend(
                dividendId  = dividendId,
                accountId   = asset.accountId,
                assetName   = asset.name,
                grossAmount = grossAmount,
                irpfPercent = irpfPercent,
                date        = date
            )
            result
                .onSuccess { closeDividendSheet() }
                .onFailure { _sheetState.value = _sheetState.value.copy(error = it.message) }
        }
    }

    // ── Renta fija: nueva posición ──────────────────────────────────────
    fun openCreateFixedIncomeSheet() {
        _sheetState.value = _sheetState.value.copy(showCreateFixedIncomeSheet = true)
    }

    fun closeCreateFixedIncomeSheet() {
        _sheetState.value = _sheetState.value.copy(showCreateFixedIncomeSheet = false)
    }

    fun saveFixedIncomePosition(position: FixedIncomePosition, event: FixedIncomeEvent) {
        viewModelScope.launch {
            createFixedIncomePosition?.invoke(position, event)
                ?.onSuccess { closeCreateFixedIncomeSheet() }
                ?.onFailure { _sheetState.value = _sheetState.value.copy(error = it.message) }
                ?: run { _sheetState.value = _sheetState.value.copy(error = "Error al crear posición de renta fija") }
        }
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val day1 = timestamp1 / (24 * 60 * 60 * 1000)
        val day2 = timestamp2 / (24 * 60 * 60 * 1000)
        return day1 == day2
    }
}
