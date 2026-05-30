package es.aviferdev.n3to.ui.portfolio.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.AssetComposition
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.model.FixedIncomeSummary
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.PortfolioScreenData
import es.aviferdev.n3to.domain.model.PortfolioValuePoint
import es.aviferdev.n3to.domain.model.PriceQuote
import es.aviferdev.n3to.domain.model.PriceSource
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.portfolio.AssetPosition
import es.aviferdev.n3to.domain.portfolio.CompoundEffect
import es.aviferdev.n3to.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetAllCompositionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsByAssetsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsByAssetsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsUseCase
import es.aviferdev.n3to.domain.usecase.assetplatform.GetPlatformsByAssetsUseCase
import es.aviferdev.n3to.domain.usecase.asset.DetectPriceAnomalyUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.n3to.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.ValidateAssetIdentifierUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.DeleteAllRegionDistributionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.DeleteAllSectorLinksUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.DeleteAssetCompositionUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetAssetCompositionUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsByAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsByAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.SaveAssetCompositionUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.SaveRegionDistributionUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.SaveSectorRelationUseCase
import es.aviferdev.n3to.domain.usecase.assetplatform.GetPlatformsByAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetplatform.LinkPlatformToAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetplatform.UnlinkAllPlatformsFromAssetUseCase
import es.aviferdev.n3to.domain.usecase.assetpricehistory.SaveAssetPriceHistoryUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.GetTransactionsByAccountUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.SaveAssetTransactionUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.SyncAssetTransactionToLedgerUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.CreateFixedIncomePositionUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetFixedIncomeSummaryUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetNearMaturityPositionsUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.RegisterCouponUseCase
import es.aviferdev.n3to.domain.usecase.issuer.CreateIssuerUseCase
import es.aviferdev.n3to.domain.usecase.issuer.GetIssuersUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.usecase.platform.SavePlatformUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfolioValueHistoryUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfoliosByAccountUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetDividendsByAssetIdsUseCase
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.portfolio.CatalogError
import es.aviferdev.n3to.ui.portfolio.PlatformError
import es.aviferdev.n3to.ui.portfolio.PortfolioStateBuilder
import es.aviferdev.n3to.ui.portfolio.PortfolioStateInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


sealed class PortfolioNewUiState {
    data class Loading(val message: String) : PortfolioNewUiState()
    data object Empty : PortfolioNewUiState()
    data object EmptyPortFolio : PortfolioNewUiState()
    data class Success(
        val data: PortfolioScreenData,
    ) : PortfolioNewUiState()

    data class Error(val message: String) : PortfolioNewUiState()
}

data class AssetRow(
    val asset: Asset,
    val position: AssetPosition
)

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
    CATEGORY, COMPOSITION, REGION, SECTOR
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
    val showCreateFixedIncomeSheet: Boolean = false,
    val currentAccountId: String? = null,
    val showRegisterCouponSheet: Boolean = false,
    val selectedPositionForCoupon: FixedIncomePosition? = null,
    val allSectors: List<AssetSector> = emptyList(),
    val allRegions: List<AssetRegion> = emptyList(),
    val compoundEffect: CompoundEffect? = null
)

data class CatalogSheetState(
    val showAddSheet: Boolean = false,
    val editing: Asset? = null,
    val editingPlatformIds: Set<String> = emptySet(),
    val editingSectorIds: Set<String> = emptySet(),
    val editingRegionPercents: Map<String, Int> = emptyMap(),
    val editingFixedIncomePercent: Int = 0,
    val error: CatalogError? = null
)

data class PlatformSheetState(
    val showAddSheet: Boolean = false,
    val error: PlatformError? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModel(
    private val getAssetsByAccount: GetAssetsByAccountUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val updateAssetCurrentPrice: UpdateAssetCurrentPriceUseCase,
    private val detectAnomaly: DetectPriceAnomalyUseCase,
    private val getAssetCategoriesIncludingArchived: GetAllAssetCategoriesIncludingArchivedUseCase,
    private val getAccountById: GetAccountByIdUseCase,
    private val getTransactionsByAccount: GetTransactionsByAccountUseCase,
    private val getPlatforms: GetPlatformsUseCase,
    private val saveAssetTransaction: SaveAssetTransactionUseCase,
    private val syncToLedger: SyncAssetTransactionToLedgerUseCase,
    private val saveAssetPriceHistory: SaveAssetPriceHistoryUseCase,
    private val getPlatformsByAssets: GetPlatformsByAssetsUseCase,
    private val getAllCompositions: GetAllCompositionsUseCase,
    private val getSectorsByAssets: GetSectorsByAssetsUseCase,
    private val getRegionsByAssets: GetRegionsByAssetsUseCase,
    private val getSectors: GetSectorsUseCase,
    private val getRegions: GetRegionsUseCase,
    private val saveAsset: SaveAssetUseCase,
    private val getPlatformsByAsset: GetPlatformsByAssetUseCase,
    private val getSectorsByAsset: GetSectorsByAssetUseCase,
    private val getRegionsByAsset: GetRegionsByAssetUseCase,
    private val getAssetComposition: GetAssetCompositionUseCase,
    private val saveAssetComposition: SaveAssetCompositionUseCase,
    private val deleteAssetComposition: DeleteAssetCompositionUseCase,
    private val deleteAllSectorLinks: DeleteAllSectorLinksUseCase,
    private val saveSectorRelation: SaveSectorRelationUseCase,
    private val deleteAllRegionDistributions: DeleteAllRegionDistributionsUseCase,
    private val saveRegionDistribution: SaveRegionDistributionUseCase,
    private val linkPlatformToAsset: LinkPlatformToAssetUseCase,
    private val unlinkAllPlatformsFromAsset: UnlinkAllPlatformsFromAssetUseCase,
    private val validateAssetIdentifier: ValidateAssetIdentifierUseCase? = null,
    private val savePlatform: SavePlatformUseCase,
    private val session: AccountSession,
    private val getFixedIncomeSummary: GetFixedIncomeSummaryUseCase,
    private val getNearMaturityPositions: GetNearMaturityPositionsUseCase? = null,
    private val createFixedIncomePosition: CreateFixedIncomePositionUseCase? = null,
    private val getBondIssuers: GetIssuersUseCase,
    private val createIssuer: CreateIssuerUseCase,
    private val getPortfolioValueHistory: GetPortfolioValueHistoryUseCase,
    private val registerCoupon: RegisterCouponUseCase? = null,
    private val getPortfoliosByAccount: GetPortfoliosByAccountUseCase,
    private val getDividendsByAssetIds: GetDividendsByAssetIdsUseCase,
    private val stateBuilder: PortfolioStateBuilder,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PortfolioNewUiState>(PortfolioNewUiState.Loading(""))
    val uiState: StateFlow<PortfolioNewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.selectedAccountId.collectLatest { accountId ->
                if (accountId == null) {
                    _uiState.value = PortfolioNewUiState.Empty
                } else {
                    getPortfoliosByAccount(accountId).collectLatest { portfolios ->
                        _uiState.value = if (portfolios.isEmpty()) {
                            PortfolioNewUiState.EmptyPortFolio
                        } else {
                            PortfolioNewUiState.Success(PortfolioScreenData(portfolios))
                        }
                    }
                }
            }
        }
    }

    private val _sheetState = MutableStateFlow(SheetState())
    private val _selectedDistributionView = MutableStateFlow(DistributionView.CATEGORY)

    // ── Portfolio (cartera) ────────────────────────────────────────────────────
    private val _selectedPortfolioId = MutableStateFlow<String?>(null)
    val selectedPortfolioId: StateFlow<String?> = _selectedPortfolioId.asStateFlow()

    fun selectPortfolio(id: String?) {
        _selectedPortfolioId.value = id
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

    private val allSectors: StateFlow<List<AssetSector>> =
        getSectors()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allRegions: StateFlow<List<AssetRegion>> =
        getRegions()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private data class SheetState(
        val showUpdatePriceSheet: Boolean = false,
        val pricingAsset: Asset? = null,
        val showAddTxSheet: Boolean = false,
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
        val compositions: List<AssetComposition>,
        val sectorRelations: List<AssetSectorRelation>,
        val regionDistributions: List<AssetRegionDistribution>
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
                            getPlatformsByAssets(assetIds),
                            getAllCompositions(),
                            getSectorsByAssets(assetIds),
                            getRegionsByAssets(assetIds)
                        ) { platformsByAsset, compositions, sectorRelations, regionDistributions ->
                            BasePortfolioData(
                                platformsByAsset,
                                compositions,
                                sectorRelations,
                                regionDistributions
                            )
                        }
                        combine(
                            metaFlow,
                            getDividendsByAssetIds(assetIds)
                        ) { meta, dividends ->
                            buildPortfolioState(
                                portfolioId,
                                basicData,
                                meta,
                                dividends,
                                accountId
                            )
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
                showCreateFixedIncomeSheet = sheets.showCreateFixedIncomeSheet,
                showRegisterCouponSheet = sheets.showRegisterCouponSheet,
                selectedPositionForCoupon = sheets.selectedPositionForCoupon,
                error = sheets.error
            )
        }
        .combine(_selectedDistributionView) { state, view ->
            state.copy(
                selectedDistributionView = view
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PortfolioUiState())

    val availableCategories: StateFlow<List<AssetCategory>> =
        getAssetCategoriesIncludingArchived()
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
                _sheetState.value =
                    _sheetState.value.copy(error = PortfolioSheetError.FiNoBuySell)
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
                    val currentAsset =
                        portfolioState.value.allAssets.find { it.id == assetId }
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
            createFixedIncomePosition?.invoke(finalPosition, event, finalPosition.id)
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
            registerCoupon?.invoke(event, accountId, position.id)
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

    // ── Catalog sheet state ───────────────────────────────────────────────────
    private val _showAddAssetSheet = MutableStateFlow(false)
    private val _editingAsset = MutableStateFlow<Asset?>(null)
    private val _editingAssetPlatformIds = MutableStateFlow<Set<String>>(emptySet())
    private val _editingAssetSectorIds = MutableStateFlow<Set<String>>(emptySet())
    private val _editingAssetRegionPercents = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _editingAssetFixedIncomePercent = MutableStateFlow(0)
    private val _catalogError = MutableStateFlow<CatalogError?>(null)

    private val _catalogSheetPart1 = combine(
        _showAddAssetSheet, _editingAsset, _editingAssetPlatformIds, _editingAssetSectorIds
    ) { show, editing, platIds, sectIds -> CatalogSheetPart1(show, editing, platIds, sectIds) }

    private val _catalogSheetPart2 = combine(
        _editingAssetRegionPercents, _editingAssetFixedIncomePercent, _catalogError
    ) { regPercents, fixedPct, error -> CatalogSheetPart2(regPercents, fixedPct, error) }

    val catalogSheetState: StateFlow<CatalogSheetState> = combine(
        _catalogSheetPart1, _catalogSheetPart2
    ) { p1, p2 ->
        CatalogSheetState(
            showAddSheet = p1.showAddSheet,
            editing = p1.editing,
            editingPlatformIds = p1.platIds,
            editingSectorIds = p1.sectIds,
            editingRegionPercents = p2.regPercents,
            editingFixedIncomePercent = p2.fixedPct,
            error = p2.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogSheetState())

    // ── Platform sheet state ──────────────────────────────────────────────────
    private val _showAddPlatformSheet = MutableStateFlow(false)
    private val _platformError = MutableStateFlow<PlatformError?>(null)

    val platformSheetState: StateFlow<PlatformSheetState> = combine(
        _showAddPlatformSheet, _platformError
    ) { showAdd, error ->
        PlatformSheetState(showAddSheet = showAdd, error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlatformSheetState())

    // ── Catalog sheet actions ─────────────────────────────────────────────────
    fun openAddAssetSheet() {
        _showAddAssetSheet.value = true
    }

    fun closeAddAssetSheet() {
        _showAddAssetSheet.value = false
    }

    fun openEditAssetSheet(asset: Asset) {
        _editingAsset.value = asset
        viewModelScope.launch {
            val platforms = getPlatformsByAsset(asset.id).first()
            _editingAssetPlatformIds.value = platforms.map { it.id }.toSet()

            val sectors = getSectorsByAsset(asset.id).first()
            _editingAssetSectorIds.value = sectors.map { it.id }.toSet()

            val regions = getRegionsByAsset(asset.id).first()
            _editingAssetRegionPercents.value = regions.associate { it.regionId to it.percent }

            val composition = getAssetComposition(asset.id).first()
            _editingAssetFixedIncomePercent.value = composition?.fixedIncomePercent ?: 0
        }
    }

    fun closeEditAssetSheet() {
        _editingAsset.value = null
        _editingAssetPlatformIds.value = emptySet()
        _editingAssetSectorIds.value = emptySet()
        _editingAssetRegionPercents.value = emptyMap()
        _editingAssetFixedIncomePercent.value = 0
    }

    fun addAsset(
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        isin: String? = null,
        platformIds: Set<String> = emptySet(),
        fixedIncomePercent: Int = 0,
        sectorIds: Set<String> = emptySet(),
        regionPercents: Map<String, Int> = emptyMap(),
        portfolioId: String? = null
    ) {
        val accountId = session.selectedAccountId.value ?: run {
            _catalogError.value = CatalogError.AccountRequired
            return
        }
        val tickerTrim = ticker.trim().uppercase()
        val nameTrim = name.trim()
        if (tickerTrim.isBlank() || nameTrim.isBlank()) {
            _catalogError.value = CatalogError.TickerAndNameRequired
            return
        }
        if (portfolioState.value.allAssets.any {
                it.ticker.equals(tickerTrim, ignoreCase = true) && it.accountId == accountId
            }) {
            _catalogError.value = CatalogError.AssetAlreadyExists(tickerTrim)
            return
        }
        viewModelScope.launch {
            val now = nowMillis()
            val asset = Asset(
                id = "asset_${now}_${(0..9999).random()}",
                accountId = accountId,
                portfolioId = portfolioId,
                ticker = tickerTrim,
                name = nameTrim,
                notes = notes?.ifBlank { null },
                createdAt = now,
                assetCategoryId = assetCategoryId,
                currentPrice = currentPrice,
                currentPriceUpdatedAt = if (currentPrice != null) now else null,
                isin = isin,
                priceSource = PriceSource.MANUAL,
                isinValidatedAt = if (isin != null) now else null,
                isinValidationError = null
            )
            saveAsset(asset)
                .onSuccess {
                    if (fixedIncomePercent > 0) {
                        saveAssetComposition(AssetComposition(
                            assetId = asset.id,
                            fixedIncomePercent = fixedIncomePercent,
                            createdAt = now
                        ))
                    }
                    sectorIds.forEach { sectorId ->
                        saveSectorRelation(AssetSectorRelation(assetId = asset.id, sectorId = sectorId))
                    }
                    regionPercents.forEach { (regionId, percent) ->
                        if (percent > 0) {
                            saveRegionDistribution(AssetRegionDistribution(
                                assetId = asset.id, regionId = regionId, percent = percent
                            ))
                        }
                    }
                    platformIds.forEach { platId -> linkPlatformToAsset(asset.id, platId) }
                }
                .onFailure { _catalogError.value = CatalogError.Unknown(it.message) }
            _showAddAssetSheet.value = false
        }
    }

    fun editAsset(
        original: Asset,
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        isin: String? = null,
        platformIds: Set<String> = emptySet(),
        fixedIncomePercent: Int = 0,
        sectorIds: Set<String> = emptySet(),
        regionPercents: Map<String, Int> = emptyMap(),
        portfolioId: String? = null
    ) {
        val tickerTrim = ticker.trim().uppercase()
        val nameTrim = name.trim()
        if (tickerTrim.isBlank() || nameTrim.isBlank()) {
            _catalogError.value = CatalogError.TickerAndNameRequired
            return
        }
        viewModelScope.launch {
            val updatedAt = when {
                currentPrice == null -> null
                currentPrice == original.currentPrice -> original.currentPriceUpdatedAt
                else -> nowMillis()
            }
            updateAsset(
                original.copy(
                    ticker = tickerTrim,
                    name = nameTrim,
                    notes = notes?.ifBlank { null },
                    assetCategoryId = assetCategoryId,
                    currentPrice = currentPrice,
                    currentPriceUpdatedAt = updatedAt,
                    portfolioId = portfolioId,
                    isin = isin,
                    priceSource = PriceSource.MANUAL,
                    isinValidatedAt = if (isin != null) nowMillis() else null,
                    isinValidationError = null
                )
            ).onSuccess {
                if (fixedIncomePercent > 0) {
                    saveAssetComposition(AssetComposition(
                        assetId = original.id,
                        fixedIncomePercent = fixedIncomePercent,
                        createdAt = nowMillis()
                    ))
                } else {
                    deleteAssetComposition(original.id)
                }
                deleteAllSectorLinks(original.id)
                sectorIds.forEach { sectorId ->
                    saveSectorRelation(AssetSectorRelation(assetId = original.id, sectorId = sectorId))
                }
                deleteAllRegionDistributions(original.id)
                regionPercents.forEach { (regionId, percent) ->
                    if (percent > 0) {
                        saveRegionDistribution(AssetRegionDistribution(
                            assetId = original.id, regionId = regionId, percent = percent
                        ))
                    }
                }
                unlinkAllPlatformsFromAsset(original.id)
                platformIds.forEach { platId -> linkPlatformToAsset(original.id, platId) }
            }.onFailure { _catalogError.value = CatalogError.Unknown(it.message) }
            _editingAsset.value = null
            _editingAssetPlatformIds.value = emptySet()
        }
    }

    suspend fun validateIsin(identifier: String, categoryId: String?): Result<PriceQuote> {
        if (validateAssetIdentifier == null || categoryId == null) {
            return Result.failure(Exception("Validación no disponible"))
        }
        return validateAssetIdentifier(identifier, categoryId)
    }

    fun clearCatalogError() {
        _catalogError.value = null
    }

    // ── Platform sheet actions ────────────────────────────────────────────────
    fun openAddPlatformSheet() {
        _showAddPlatformSheet.value = true
    }

    fun closeAddPlatformSheet() {
        _showAddPlatformSheet.value = false
    }

    fun addPlatform(name: String, icon: String, notes: String?) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (portfolioState.value.platforms.any { it.name.equals(trimmed, ignoreCase = true) }) {
            _platformError.value = PlatformError.AlreadyExists
            return
        }
        val validatedNotes = notes?.take(200)?.ifBlank { null }
        viewModelScope.launch {
            val now = nowMillis()
            val nextOrder = (portfolioState.value.platforms.maxOfOrNull { it.sortOrder } ?: -1) + 1
            savePlatform(Platform(
                id = "platform_$now",
                name = trimmed,
                icon = icon.ifBlank { "🏦" },
                sortOrder = nextOrder,
                createdAt = now,
                notes = validatedNotes
            )).onFailure { _platformError.value = PlatformError.Unknown(it.message) }
            _showAddPlatformSheet.value = false
        }
    }

    fun clearPlatformError() {
        _platformError.value = null
    }

    private data class CatalogSheetPart1(
        val showAddSheet: Boolean, val editing: Asset?,
        val platIds: Set<String>, val sectIds: Set<String>
    )

    private data class CatalogSheetPart2(
        val regPercents: Map<String, Int>, val fixedPct: Int, val error: CatalogError?
    )
}
