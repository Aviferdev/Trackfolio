package es.aviferdev.n3to.ui.portfolio

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.model.FixedIncomeSummary
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.model.PortfolioValuePoint
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.portfolio.AssetPosition
import es.aviferdev.n3to.domain.portfolio.CompoundEffect
import es.aviferdev.n3to.domain.repository.AssetMetadataRepository
import es.aviferdev.n3to.domain.repository.AssetPlatformRepository
import es.aviferdev.n3to.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.n3to.domain.usecase.asset.ArchiveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.DetectPriceAnomalyUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.n3to.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.assetpricehistory.SaveAssetPriceHistoryUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.GetTransactionsByAccountUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.SaveAssetTransactionUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.SyncAssetTransactionToLedgerUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.CreateFixedIncomePositionUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetFixedIncomeSummaryUseCase
import es.aviferdev.n3to.domain.usecase.issuer.CreateIssuerUseCase
import es.aviferdev.n3to.domain.usecase.issuer.GetIssuersUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.DeletePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfolioValueHistoryUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfoliosByAccountUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.SavePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetDividendsByAssetIdsUseCase
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.account.AccountSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ─── Estado de cada activo enriquecido con su posición FIFO ──────────────────
data class AssetRow(
    val asset: Asset,
    val position: AssetPosition
)

/** Grupo de activos abiertos pertenecientes a la misma categoría (o "Sin categoría"). */
data class CategoryGroup(
    val category: AssetCategory?,
    val customName: String? = null,
    val rows: List<AssetRow>,
    val fixedIncomeRows: List<FixedIncomeRow> = emptyList(),
    val totalInvested: Double,
    val totalCurrentValue: Double,
    val totalUnrealizedPnL: Double,
    val totalRealizedPnL: Double,
    val totalPnL: Double,
    val totalPnLPercent: Double
) {
    val displayName: String get() = customName ?: category?.name ?: "Sin categoría"
    val displayIcon: String get() = category?.icon ?: "❔"
    val sortKey: Int get() = category?.sortOrder ?: Int.MAX_VALUE
    val rowCount: Int get() = rows.size + fixedIncomeRows.size
}

data class CategorySlice(
    val categoryId: String?,
    val name: String,
    val icon: String,
    val value: Double,
    val percent: Double,
    val color: Color
)

enum class DistributionView {
    CATEGORY,
    COMPOSITION,
    REGION,
    SECTOR;

    val displayName: String
        get() = when (this) {
            CATEGORY -> "Categoría"
            COMPOSITION -> "Composición"
            REGION -> "Región"
            SECTOR -> "Sector"
        }
}

sealed class PortfolioSheetError {
    data object FiNoBuySell : PortfolioSheetError()
    data class PriceHistorySave(val message: String) : PortfolioSheetError()
    data object AssetNotFound : PortfolioSheetError()
    data object FiCreatePosition : PortfolioSheetError()
    data object CouponRegister : PortfolioSheetError()
    data object NoAccountSelected : PortfolioSheetError()
    data class Unknown(val message: String?) : PortfolioSheetError()
}

data class PortfolioUiState(
    val groups: List<CategoryGroup> = emptyList(),
    val regionGroups: List<CategoryGroup> = emptyList(),
    val sectorGroups: List<CategoryGroup> = emptyList(),
    val closedPositions: List<AssetRow> = emptyList(),
    val closedFixedIncomePositions: List<FixedIncomeRow> = emptyList(),
    val distribution: List<CategorySlice> = emptyList(),
    val compositionDistribution: List<CategorySlice> = emptyList(),
    val regionDistribution: List<CategorySlice> = emptyList(),
    val sectorDistribution: List<CategorySlice> = emptyList(),
    val selectedDistributionView: DistributionView = DistributionView.CATEGORY,
    val totalInvested: Double = 0.0,
    val totalCurrentValue: Double = 0.0,
    val totalRealizedPnL: Double = 0.0,
    val totalUnrealizedPnL: Double = 0.0,
    val totalPnL: Double = 0.0,
    val totalPnLPercent: Double = 0.0,
    val openPositionsCount: Int = 0,
    val fixedIncomeSummary: FixedIncomeSummary? = null,
    val nearMaturityPositions: List<FixedIncomePosition> = emptyList(),
    val combinedInvested: Double = 0.0,
    val combinedCurrentValue: Double = 0.0,
    val combinedPnL: Double = 0.0,
    val combinedPnLPercent: Double = 0.0,
    val combinedRealizedPnL: Double = 0.0,
    val combinedUnrealizedPnL: Double = 0.0,
    val allAssets: List<Asset> = emptyList(),
    val platforms: List<Platform> = emptyList(),
    val platformsByAsset: Map<String, List<Platform>> = emptyMap(),
    val bondIssuers: List<Issuer> = emptyList(),
    val bankIssuers: List<Issuer> = emptyList(),
    val isLoading: Boolean = true,
    val error: PortfolioSheetError? = null,
    val showUpdatePriceSheet: Boolean = false,
    val pricingAsset: Asset? = null,
    val showAddTxSheet: Boolean = false,
    val showDividendSheet: Boolean = false,
    val dividendAssetId: String? = null,
    val showCreateFixedIncomeSheet: Boolean = false,
    val currentAccountId: String? = null,
    val showRegisterCouponSheet: Boolean = false,
    val selectedPositionForCoupon: FixedIncomePosition? = null,
    val allSectors: List<es.aviferdev.n3to.domain.model.AssetSector> = emptyList(),
    val allRegions: List<es.aviferdev.n3to.domain.model.AssetRegion> = emptyList(),
    val compoundEffect: CompoundEffect? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModel(
    private val getAssetsByAccount: GetAssetsByAccountUseCase,
    private val saveAsset: SaveAssetUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase,
    private val archiveAsset: ArchiveAssetUseCase,
    private val detectAnomaly: DetectPriceAnomalyUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val getAccountById: GetAccountByIdUseCase,
    private val getTransactionsByAccount: GetTransactionsByAccountUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val saveAssetTransaction: SaveAssetTransactionUseCase,
    private val syncToLedger: SyncAssetTransactionToLedgerUseCase,
    private val saveAssetPriceHistory: SaveAssetPriceHistoryUseCase,
    private val assetPlatformRepository: AssetPlatformRepository,
    private val assetMetadataRepository: AssetMetadataRepository,
    private val session: AccountSession,
    private val getFixedIncomeSummary: GetFixedIncomeSummaryUseCase,
    private val getNearMaturityPositions: es.aviferdev.n3to.domain.usecase.fixedincome.GetNearMaturityPositionsUseCase? = null,
    private val createFixedIncomePosition: CreateFixedIncomePositionUseCase? = null,
    private val getBondIssuers: GetIssuersUseCase,
    private val createIssuer: CreateIssuerUseCase,
    private val getPortfolioValueHistory: GetPortfolioValueHistoryUseCase,
    private val registerCoupon: es.aviferdev.n3to.domain.usecase.fixedincome.RegisterCouponUseCase? = null,
    private val getPortfoliosByAccount: GetPortfoliosByAccountUseCase,
    private val savePortfolio: SavePortfolioUseCase,
    private val deletePortfolio: DeletePortfolioUseCase,
    private val getDividendsByAssetIds: GetDividendsByAssetIdsUseCase,
    private val stateBuilder: PortfolioStateBuilder
) : ViewModel() {

    private val _sheetState = MutableStateFlow(SheetState())
    private val _selectedDistributionView = MutableStateFlow(DistributionView.CATEGORY)

    // ── Portfolio (cartera) ────────────────────────────────────────────────────
    private val _selectedPortfolioId = MutableStateFlow<String?>(null)
    val selectedPortfolioId: StateFlow<String?> = _selectedPortfolioId.asStateFlow()

    private val _showAddPortfolioSheet = MutableStateFlow(false)
    val showAddPortfolioSheet: StateFlow<Boolean> = _showAddPortfolioSheet.asStateFlow()

    private val _portfolios: StateFlow<List<Portfolio>> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(emptyList())
            else getPortfoliosByAccount(accountId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val portfolios: StateFlow<List<Portfolio>> = _portfolios

    init {
        viewModelScope.launch {
            portfolios.collect { list ->
                val currentId = _selectedPortfolioId.value
                if (currentId != null && list.none { it.id == currentId }) {
                    _selectedPortfolioId.value = null
                }
            }
        }
    }

    fun selectPortfolio(id: String?) {
        _selectedPortfolioId.value = id
    }

    fun openAddPortfolioSheet() {
        _showAddPortfolioSheet.value = true
    }

    fun closeAddPortfolioSheet() {
        _showAddPortfolioSheet.value = false
    }

    fun addPortfolio(name: String, description: String?) {
        val accountId = session.selectedAccountId.value ?: return
        viewModelScope.launch {
            savePortfolio(accountId, name, description)
            _showAddPortfolioSheet.value = false
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val portfolioValueHistory: StateFlow<List<PortfolioValuePoint>> = combine(
        session.selectedAccountId,
        _selectedPortfolioId
    ) { accountId, portfolioId -> accountId to portfolioId }
        .flatMapLatest { (accountId, portfolioId) ->
            if (accountId == null) flowOf(emptyList())
            else getPortfolioValueHistory(accountId, portfolioId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allSectors: StateFlow<List<es.aviferdev.n3to.domain.model.AssetSector>> =
        assetMetadataRepository.getAllSectors()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allRegions: StateFlow<List<es.aviferdev.n3to.domain.model.AssetRegion>> =
        assetMetadataRepository.getAllRegions()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private data class SheetState(
        val showUpdatePriceSheet: Boolean = false,
        val pricingAsset: Asset? = null,
        val showAddTxSheet: Boolean = false,
        val showDividendSheet: Boolean = false,
        val dividendAssetId: String? = null,
        val showCreateFixedIncomeSheet: Boolean = false,
        val showRegisterCouponSheet: Boolean = false,
        val selectedPositionForCoupon: FixedIncomePosition? = null,
        val error: PortfolioSheetError? = null
    )

    private data class BasicPortfolioData(
        val assets: List<Asset>,
        val categories: List<AssetCategory>,
        val account: Account?,
        val transactions: List<AssetTransaction>,
        val platforms: List<Platform>
    )

    private data class BasePortfolioData(
        val platformsByAsset: Map<String, List<Platform>>,
        val compositions: List<es.aviferdev.n3to.domain.model.AssetComposition>,
        val sectorRelations: List<es.aviferdev.n3to.domain.model.AssetSectorRelation>,
        val regionDistributions: List<es.aviferdev.n3to.domain.model.AssetRegionDistribution>
    )

    private data class BasicPortfolioDataWithFI(
        val assets: List<Asset>,
        val categories: List<AssetCategory>,
        val account: Account?,
        val transactions: List<AssetTransaction>,
        val platforms: List<Platform>,
        val fiSummary: FixedIncomeSummary?,
        val nearMaturityPositions: List<FixedIncomePosition>,
        val bondIssuers: List<Issuer> = emptyList(),
        val bankIssuers: List<Issuer> = emptyList()
    )

    val portfolioState: StateFlow<PortfolioUiState> = combine(
        session.selectedAccountId,
        _selectedPortfolioId
    ) { accountId, portfolioId -> accountId to portfolioId }
        .flatMapLatest { (accountId, portfolioId) ->
            if (accountId == null) {
                flowOf(PortfolioUiState(isLoading = false))
            } else {
                val nearMaturityFlow =
                    getNearMaturityPositions?.invoke(accountId) ?: flowOf(emptyList())
                val bondIssuersFlow = getBondIssuers(accountId, IssuerType.BOND_ISSUER)
                val bankIssuersFlow = getBondIssuers(accountId, IssuerType.BANK)

                val baseDataFlow = combine(
                    getAssetsByAccount(accountId),
                    getAssetCategoriesIncludingArchived(),
                    getAccountById(accountId),
                    getTransactionsByAccount(accountId),
                    getPlatforms()
                ) { assets, categories, account, txs, platforms ->
                    BasicPortfolioData(assets, categories, account, txs, platforms)
                }

                combine(
                    baseDataFlow,
                    getFixedIncomeSummary(accountId),
                    nearMaturityFlow,
                    bondIssuersFlow,
                    bankIssuersFlow
                ) { baseData, fiSummary, nearMaturity, bondIssuers, bankIssuers ->
                    BasicPortfolioDataWithFI(
                        assets = baseData.assets,
                        categories = baseData.categories,
                        account = baseData.account,
                        transactions = baseData.transactions,
                        platforms = baseData.platforms,
                        fiSummary = fiSummary,
                        nearMaturityPositions = nearMaturity,
                        bondIssuers = bondIssuers,
                        bankIssuers = bankIssuers
                    )
                }.flatMapLatest { basicData ->
                    val assetIds = basicData.assets.map { it.id }
                    if (assetIds.isEmpty()) {
                        flowOf(
                            buildPortfolioState(
                                portfolioId,
                                basicData,
                                BasePortfolioData(
                                    emptyMap(),
                                    emptyList(),
                                    emptyList(),
                                    emptyList()
                                ),
                                emptyMap(),
                                accountId
                            )
                        )
                    } else {
                        val metaFlow = combine(
                            assetPlatformRepository.getPlatformsByAssets(assetIds),
                            assetMetadataRepository.getAllCompositions(),
                            assetMetadataRepository.getSectorsByAssetIds(assetIds),
                            assetMetadataRepository.getRegionDistributionsByAssetIds(assetIds)
                        ) { platformsByAsset, compositions, sectorRelations, regionDistributions ->
                            BasePortfolioData(
                                platformsByAsset,
                                compositions,
                                sectorRelations,
                                regionDistributions
                            )
                        }
                        combine(metaFlow, getDividendsByAssetIds(assetIds)) { meta, dividends ->
                            buildPortfolioState(portfolioId, basicData, meta, dividends, accountId)
                        }
                    }
                }
            }
        }
        .combine(_sheetState) { state, sheets ->
            state.copy(
                showUpdatePriceSheet = sheets.showUpdatePriceSheet,
                pricingAsset = sheets.pricingAsset,
                showAddTxSheet = sheets.showAddTxSheet,
                showDividendSheet = sheets.showDividendSheet,
                dividendAssetId = sheets.dividendAssetId,
                showCreateFixedIncomeSheet = sheets.showCreateFixedIncomeSheet,
                showRegisterCouponSheet = sheets.showRegisterCouponSheet,
                selectedPositionForCoupon = sheets.selectedPositionForCoupon,
                error = sheets.error
            )
        }
        .combine(_selectedDistributionView) { state, view -> state.copy(selectedDistributionView = view) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PortfolioUiState())

    val availableCategories: StateFlow<List<AssetCategory>> = getAssetCategoriesIncludingArchived()
        .map { list -> list.filter { !it.archived } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun buildPortfolioState(
        portfolioId: String?,
        basicData: BasicPortfolioDataWithFI,
        meta: BasePortfolioData,
        dividendsByAsset: Map<String, List<Transaction>>,
        accountId: String?
    ): PortfolioUiState = stateBuilder.build(
        PortfolioStateInput(
            portfolioId = portfolioId,
            assets = basicData.assets,
            categories = basicData.categories,
            account = basicData.account,
            transactions = basicData.transactions,
            platforms = basicData.platforms,
            platformsByAsset = meta.platformsByAsset,
            fiSummary = basicData.fiSummary,
            nearMaturityPositions = basicData.nearMaturityPositions,
            accountId = accountId,
            compositions = meta.compositions,
            sectorRelations = meta.sectorRelations,
            regionDistributions = meta.regionDistributions,
            bondIssuers = basicData.bondIssuers,
            bankIssuers = basicData.bankIssuers,
            dividendsByAsset = dividendsByAsset,
            allSectors = allSectors.value,
            allRegions = allRegions.value,
            selectedDistributionView = _selectedDistributionView.value
        )
    )

    // ── Sheet de actualización de precio ──────────────────────────────────────
    fun openUpdatePriceSheet(asset: Asset) {
        _sheetState.value =
            _sheetState.value.copy(showUpdatePriceSheet = true, pricingAsset = asset)
    }

    fun closeUpdatePriceSheet() {
        _sheetState.value =
            _sheetState.value.copy(showUpdatePriceSheet = false, pricingAsset = null)
    }

    fun selectDistributionView(view: DistributionView) {
        _selectedDistributionView.value = view
    }

    fun refreshCurrentPrice(asset: Asset, newPrice: Double) {
        viewModelScope.launch {
            val now = nowMillis()
            updateAssetCurrentPrice(asset.id, newPrice, now, asset.assetCategoryId)
                .onSuccess {
                    getPortfolioValueHistory.triggerRefresh()
                    closeUpdatePriceSheet()
                }
                .onFailure {
                    _sheetState.value =
                        _sheetState.value.copy(error = PortfolioSheetError.Unknown(it.message))
                }
        }
    }

    // ── Sheet de "Nuevo movimiento" ───────────────────────────────────────────
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
        notes: String?,
        portfolioId: String? = null
    ) {
        viewModelScope.launch {
            val asset = portfolioState.value.allAssets.find { it.id == assetId }
            if (asset != null && AssetCategoryType.isFixedIncome(asset.assetCategoryId)) {
                _sheetState.value = _sheetState.value.copy(error = PortfolioSheetError.FiNoBuySell)
                return@launch
            }

            val now = nowMillis()
            val tx = AssetTransaction(
                id = "tx_${now}_${(0..9999).random()}",
                assetId = assetId,
                type = type,
                quantity = quantity,
                pricePerUnit = pricePerUnit,
                date = date,
                platformId = platformId,
                feeNote = feeNote?.ifBlank { null },
                notes = notes?.ifBlank { null },
                createdAt = now
            )
            saveAssetTransaction(tx)
                .onSuccess {
                    val currentAsset = portfolioState.value.allAssets.find { it.id == assetId }
                    if (currentAsset != null) {
                        if (portfolioId != null && currentAsset.portfolioId != portfolioId) {
                            updateAsset(currentAsset.copy(portfolioId = portfolioId))
                        }
                        syncToLedger.sync(
                            assetTx = tx,
                            accountId = currentAsset.accountId,
                            assetName = currentAsset.name
                        )
                        if (type == AssetTransactionType.BUY) {
                            saveAssetPriceHistory(assetId, pricePerUnit, date)
                                .onFailure { err ->
                                    _sheetState.value = _sheetState.value.copy(
                                        error = PortfolioSheetError.PriceHistorySave(
                                            err.message ?: ""
                                        )
                                    )
                                }
                        }
                    }
                    closeAddTransactionSheet()
                }
                .onFailure {
                    _sheetState.value =
                        _sheetState.value.copy(error = PortfolioSheetError.Unknown(it.message))
                }
        }
    }

    fun clearError() {
        _sheetState.value = _sheetState.value.copy(error = null)
    }

    /** Acceso al detector de anomalías para el sheet de actualización de precio. */
    val priceAnomalyDetector: DetectPriceAnomalyUseCase get() = detectAnomaly

    // ── Dividendos ────────────────────────────────────────────────────────────
    fun openDividendSheet() {
        _sheetState.value = _sheetState.value.copy(showDividendSheet = true, dividendAssetId = null)
    }

    fun closeDividendSheet() {
        _sheetState.value =
            _sheetState.value.copy(showDividendSheet = false, dividendAssetId = null)
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
                _sheetState.value =
                    _sheetState.value.copy(error = PortfolioSheetError.AssetNotFound)
                return@launch
            }
            val dividendId = "div_${nowMillis()}_${(0..9999).random()}"
            syncToLedger.syncDividend(
                dividendId = dividendId,
                accountId = asset.accountId,
                assetName = asset.name,
                grossAmount = grossAmount,
                withholdingPercent = irpfPercent,
                date = date
            )
                .onSuccess { closeDividendSheet() }
                .onFailure {
                    _sheetState.value =
                        _sheetState.value.copy(error = PortfolioSheetError.Unknown(it.message))
                }
        }
    }

    // ── Renta fija: nueva posición ────────────────────────────────────────────
    fun openCreateFixedIncomeSheet() {
        _sheetState.value = _sheetState.value.copy(showCreateFixedIncomeSheet = true)
    }

    fun closeCreateFixedIncomeSheet() {
        _sheetState.value = _sheetState.value.copy(showCreateFixedIncomeSheet = false)
    }

    fun saveFixedIncomePosition(position: FixedIncomePosition, event: FixedIncomeEvent) {
        viewModelScope.launch {
            val portfolioId = _selectedPortfolioId.value
            val finalPosition = if (portfolioId != null && position.portfolioId == null) {
                position.copy(portfolioId = portfolioId)
            } else position
            createFixedIncomePosition?.invoke(finalPosition, event)
                ?.onSuccess { closeCreateFixedIncomeSheet() }
                ?.onFailure {
                    _sheetState.value =
                        _sheetState.value.copy(error = PortfolioSheetError.Unknown(it.message))
                }
                ?: run {
                    _sheetState.value =
                        _sheetState.value.copy(error = PortfolioSheetError.FiCreatePosition)
                }
        }
    }

    // ── Renta fija: registrar cupón ───────────────────────────────────────────
    fun showRegisterCouponSheet(position: FixedIncomePosition) {
        _sheetState.value = _sheetState.value.copy(
            showRegisterCouponSheet = true,
            selectedPositionForCoupon = position
        )
    }

    fun hideRegisterCouponSheet() {
        _sheetState.value = _sheetState.value.copy(
            showRegisterCouponSheet = false,
            selectedPositionForCoupon = null
        )
    }

    fun registerCoupon(event: FixedIncomeEvent) {
        val position = _sheetState.value.selectedPositionForCoupon ?: return
        val accountId = session.selectedAccountId.value ?: return
        viewModelScope.launch {
            registerCoupon?.invoke(event, accountId)
                ?.onSuccess { hideRegisterCouponSheet() }
                ?.onFailure {
                    _sheetState.value =
                        _sheetState.value.copy(error = PortfolioSheetError.Unknown(it.message))
                }
                ?: run {
                    _sheetState.value =
                        _sheetState.value.copy(error = PortfolioSheetError.CouponRegister)
                }
        }
    }

    fun saveBondIssuer(name: String, icon: String, type: IssuerType) {
        viewModelScope.launch {
            val accountId = session.selectedAccountId.value
            if (accountId == null) {
                _sheetState.value =
                    _sheetState.value.copy(error = PortfolioSheetError.NoAccountSelected)
                return@launch
            }
            createIssuer(accountId, name, icon, type, nowMillis())
                .onFailure {
                    _sheetState.value =
                        _sheetState.value.copy(error = PortfolioSheetError.Unknown(it.message))
                }
        }
    }
}
