package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
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
    // Sheet de adquisición de renta fija desde el FAB del Portfolio
    val showAcquireFixedIncomeSheet: Boolean = false,
    val acquireFixedIncomeAsset: Asset? = null,
    // Sheet de dividendo desde el FAB del Portfolio
    val showDividendSheet: Boolean      = false,
    val dividendAssetId: String?        = null,
    // Sheet de bono/depósito desde el FAB del Portfolio
    val showBondDepositSheet: Boolean   = false,
    val bondDepositAssetId: String?     = null
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
    private val session: AccountSession
) : ViewModel() {

    private val _sheetState = MutableStateFlow(SheetState())

    private data class SheetState(
        val showUpdatePriceSheet: Boolean = false,
        val pricingAsset: Asset? = null,
        val showAddTxSheet: Boolean = false,
        val showAcquireFixedIncomeSheet: Boolean = false,
        val acquireFixedIncomeAsset: Asset? = null,
        val showDividendSheet: Boolean = false,
        val dividendAssetId: String? = null,
        val showBondDepositSheet: Boolean = false,
        val bondDepositAssetId: String? = null,
        val error: String? = null
    )

    private data class BasicPortfolioData(
        val assets: List<Asset>,
        val categories: List<AssetCategory>,
        val account: Account?,
        val transactions: List<AssetTransaction>,
        val platforms: List<Platform>
    )

    val portfolioState: StateFlow<PortfolioUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) {
                flowOf(PortfolioUiState(isLoading = false))
            } else {
                combine(
                    getAssetsByAccount(accountId),
                    getAssetCategoriesIncludingArchived(),
                    getAccountById(accountId),
                    getTransactionsByAccount(accountId),
                    getPlatforms(),
                ) { assets, categories, account, txs, platforms ->
                    BasicPortfolioData(assets, categories, account, txs, platforms)
                }.flatMapLatest { basicData ->
                    val assetIds = basicData.assets.map { it.id }
                    if (assetIds.isEmpty()) {
                        flowOf(buildState(basicData.assets, basicData.categories, basicData.account, basicData.transactions, basicData.platforms, emptyMap()))
                    } else {
                        assetPlatformRepository.getPlatformsByAssets(assetIds).map { platformsByAsset ->
                            buildState(basicData.assets, basicData.categories, basicData.account, basicData.transactions, basicData.platforms, platformsByAsset)
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
                showAcquireFixedIncomeSheet = sheets.showAcquireFixedIncomeSheet,
                acquireFixedIncomeAsset     = sheets.acquireFixedIncomeAsset,
                showDividendSheet           = sheets.showDividendSheet,
                dividendAssetId             = sheets.dividendAssetId,
                showBondDepositSheet        = sheets.showBondDepositSheet,
                bondDepositAssetId          = sheets.bondDepositAssetId,
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
        platformsByAsset: Map<String, List<Platform>>
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

        val distribution: List<CategorySlice> = if (totalCurrentValue <= 0.0) {
            emptyList()
        } else {
            groups
                .filter { it.totalCurrentValue > 0.0 }
                .mapIndexed { idx, g ->
                    CategorySlice(
                        categoryId = g.category?.id,
                        name       = g.displayName,
                        icon       = g.displayIcon,
                        value      = g.totalCurrentValue,
                        percent    = (g.totalCurrentValue / totalCurrentValue) * 100.0,
                        color      = colorForGroup(g, idx)
                    )
                }
                .sortedByDescending { it.percent }
        }

        // Para el % total tomamos como base el invertido remanente — es lo
        // que el usuario tiene "vivo". Si solo quedan posiciones cerradas y
        // todo se vendió, mostramos el % sobre el bruto invertido histórico.
        val pnlBase = if (totalInvested > 0.0) totalInvested
                      else closedRows.sumOf { row -> row.position.realizedPnL.let { 0.0 } } // fallback noop

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
            isLoading          = false
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

    // ── Sheet de adquisición de renta fija desde Portfolio ─────────────
    fun openAcquireFixedIncomeSheet(asset: Asset) {
        _sheetState.value = _sheetState.value.copy(
            showAcquireFixedIncomeSheet = true,
            acquireFixedIncomeAsset = asset
        )
    }

    fun closeAcquireFixedIncomeSheet() {
        _sheetState.value = _sheetState.value.copy(
            showAcquireFixedIncomeSheet = false,
            acquireFixedIncomeAsset = null
        )
    }

    fun addFixedIncomeAcquisition(
        assetId: String,
        quantity: Double,
        nominalPerUnit: Double,
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
                type         = AssetTransactionType.BUY,
                quantity     = quantity,
                pricePerUnit = nominalPerUnit,
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
                        syncToLedger.sync(assetTx = tx, accountId = asset.accountId, assetName = asset.name)
                    }
                    closeAcquireFixedIncomeSheet()
                }
                .onFailure { _sheetState.value = _sheetState.value.copy(error = it.message) }
        }
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
                    // Sincronizar con el libro de liquidez
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

    // ── Bonos / Depósitos ─────────────────────────────────────────
    fun openBondDepositSheet() {
        _sheetState.value = _sheetState.value.copy(showBondDepositSheet = true, bondDepositAssetId = null)
    }

    fun closeBondDepositSheet() {
        _sheetState.value = _sheetState.value.copy(showBondDepositSheet = false, bondDepositAssetId = null)
    }

    fun saveBondDeposit(
        assetId: String,
        grossAmount: Double,
        irpfPercent: Double,
        commissionAmount: Double,
        date: Long
    ) {
        viewModelScope.launch {
            val asset = portfolioState.value.allAssets.find { it.id == assetId }
            if (asset == null) {
                _sheetState.value = _sheetState.value.copy(error = "Activo no encontrado")
                return@launch
            }
            val bondDepositId = "bond_${Clock.System.now().toEpochMilliseconds()}_${(0..9999).random()}"
            val result = syncToLedger.syncBondDeposit(
                bondDepositId    = bondDepositId,
                accountId        = asset.accountId,
                assetName        = asset.name,
                grossAmount      = grossAmount,
                irpfPercent      = irpfPercent,
                commissionAmount = commissionAmount,
                date             = date
            )
            result
                .onSuccess { closeBondDepositSheet() }
                .onFailure { _sheetState.value = _sheetState.value.copy(error = it.message) }
        }
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val day1 = timestamp1 / (24 * 60 * 60 * 1000)
        val day2 = timestamp2 / (24 * 60 * 60 * 1000)
        return day1 == day2
    }
}
