package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuid4
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.AssetPriceHistory
import es.aviferdev.trackfolio.domain.model.AssetCategoryType
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import es.aviferdev.trackfolio.domain.model.FixedIncomeRow
import es.aviferdev.trackfolio.domain.model.FixedIncomeSummary
import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.IssuerType
import es.aviferdev.trackfolio.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.repository.AssetMetadataRepository
import es.aviferdev.trackfolio.domain.repository.AssetPlatformRepository
import es.aviferdev.trackfolio.domain.repository.AssetPriceHistoryRepository
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
import es.aviferdev.trackfolio.domain.usecase.issuer.GetIssuersUseCase
import es.aviferdev.trackfolio.domain.usecase.issuer.SaveIssuerUseCase
import es.aviferdev.trackfolio.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.trackfolio.domain.usecase.portfolio.GetPortfolioValueHistoryUseCase
import es.aviferdev.trackfolio.domain.model.PortfolioValuePoint
import es.aviferdev.trackfolio.ui.account.AccountSession
import es.aviferdev.trackfolio.ui.theme.CategoryPalette
import es.aviferdev.trackfolio.ui.theme.PositiveGreen
import es.aviferdev.trackfolio.ui.theme.UncategorizedColor
import es.aviferdev.trackfolio.ui.theme.WarnAmber
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
    val customName: String? = null,  // Para grupos por región/sector
    val rows: List<AssetRow>,
    val fixedIncomeRows: List<FixedIncomeRow> = emptyList(),
    val totalInvested: Double,           // coste de las unidades aún en cartera
    val totalCurrentValue: Double,       // valor actual de mercado
    val totalUnrealizedPnL: Double,      // P&L latente
    val totalRealizedPnL: Double,        // P&L cerrado por ventas (para mostrar)
    val totalPnL: Double,                // realized + unrealized
    val totalPnLPercent: Double          // sobre el invertido remanente
) {
    val displayName: String   get() = customName ?: category?.name ?: "Sin categoría"
    val displayIcon: String   get() = category?.icon ?: "❔"
    val sortKey: Int          get() = category?.sortOrder ?: Int.MAX_VALUE
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
    CATEGORY,    // Distribución por categoría (default)
    COMPOSITION, // RF/RV composición
    REGION,      // Distribución por región
    SECTOR;      // Distribución por sector

    val displayName: String
        get() = when (this) {
            CATEGORY    -> "Categoría"
            COMPOSITION -> "Composición"
            REGION      -> "Región"
            SECTOR      -> "Sector"
        }
}

data class PortfolioUiState(
    val groups: List<CategoryGroup>     = emptyList(),         // posiciones abiertas (agrupadas por categoría)
    val regionGroups: List<CategoryGroup> = emptyList(),       // posiciones abiertas agrupadas por región
    val sectorGroups: List<CategoryGroup> = emptyList(),        // posiciones abiertas agrupadas por sector
    val closedPositions: List<AssetRow> = emptyList(),         // qty==0 y al menos una venta registrada
    val closedFixedIncomePositions: List<FixedIncomeRow> = emptyList(), // posiciones de renta fija cerradas
    val distribution: List<CategorySlice> = emptyList(),
    val compositionDistribution: List<CategorySlice> = emptyList(),
    val regionDistribution: List<CategorySlice> = emptyList(),
    val sectorDistribution: List<CategorySlice> = emptyList(),
    val selectedDistributionView: DistributionView = DistributionView.CATEGORY,
    val totalInvested: Double           = 0.0,
    val totalCurrentValue: Double       = 0.0,
    val totalRealizedPnL: Double        = 0.0,
    val totalUnrealizedPnL: Double      = 0.0,
    val totalPnL: Double                = 0.0,
    val totalPnLPercent: Double         = 0.0,
    val openPositionsCount: Int         = 0,
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
    /** Emisores de renta fija (BOND_ISSUER) para bonos y letras. */
    val bondIssuers: List<Issuer>       = emptyList(),
    /** Entidades bancarias (BANK) para depósitos. */
    val bankIssuers: List<Issuer>       = emptyList(),
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
    val currentAccountId: String?           = null,

    // Sheet de registrar cupón de renta fija
    val showRegisterCouponSheet: Boolean = false,
    val selectedPositionForCoupon: FixedIncomePosition? = null,

    // Sectores y regiones para sheets de activos
    val allSectors: List<es.aviferdev.trackfolio.domain.model.AssetSector> = emptyList(),
    val allRegions: List<es.aviferdev.trackfolio.domain.model.AssetRegion> = emptyList()
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
    private val assetMetadataRepository: AssetMetadataRepository,
    private val assetPriceHistoryRepository: AssetPriceHistoryRepository,
    private val session: AccountSession,
    private val getFixedIncomeSummary: GetFixedIncomeSummaryUseCase,
    private val getNearMaturityPositions: es.aviferdev.trackfolio.domain.usecase.fixedincome.GetNearMaturityPositionsUseCase? = null,
    private val createFixedIncomePosition: CreateFixedIncomePositionUseCase? = null,
    private val getBondIssuers: GetIssuersUseCase,
    private val saveBondIssuer: SaveIssuerUseCase,
    private val getPortfolioValueHistory: GetPortfolioValueHistoryUseCase,
    private val registerCoupon: es.aviferdev.trackfolio.domain.usecase.fixedincome.RegisterCouponUseCase? = null
) : ViewModel() {

    private val _sheetState = MutableStateFlow(SheetState())
    private val _selectedDistributionView = MutableStateFlow(DistributionView.CATEGORY)

    /** Historial de valor mensual del portfolio (inversiones + renta fija). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val portfolioValueHistory: StateFlow<List<PortfolioValuePoint>> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(emptyList())
            else getPortfolioValueHistory(accountId)
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val allSectors: StateFlow<List<es.aviferdev.trackfolio.domain.model.AssetSector>> =
        assetMetadataRepository.getAllSectors()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allRegions: StateFlow<List<es.aviferdev.trackfolio.domain.model.AssetRegion>> =
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
        val fixedIncomeSummary: FixedIncomeSummary? = null,
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
        val nearMaturityPositions: List<es.aviferdev.trackfolio.domain.model.FixedIncomePosition>,
        val bondIssuers: List<Issuer> = emptyList(),
        val bankIssuers: List<Issuer> = emptyList()
    )

    val portfolioState: StateFlow<PortfolioUiState> = session.selectedAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) {
                flowOf(PortfolioUiState(isLoading = false))
            } else {
                val fiFlow = getFixedIncomeSummary(accountId)

                val nearMaturityFlow = if (getNearMaturityPositions != null) {
                    getNearMaturityPositions(accountId)
                } else flowOf(emptyList())

                // Emisores de renta fija para el selector de entidad
                val bondIssuersFlow = getBondIssuers(accountId, IssuerType.BOND_ISSUER)
                val bankIssuersFlow  = getBondIssuers(accountId, IssuerType.BANK)

                val baseDataFlow = combine(
                    getAssetsByAccount(accountId),
                    getAssetCategoriesIncludingArchived(),
                    getAccountById(accountId),
                    getTransactionsByAccount(accountId),
                    getPlatforms()
                ) { assets, categories, account, txs, platforms ->
                    BasicPortfolioData(assets, categories, account, txs, platforms)
                }

                combine(baseDataFlow, fiFlow, nearMaturityFlow, bondIssuersFlow, bankIssuersFlow) { baseData, fiSummary, nearMaturity, bondIssuers, bankIssuers ->
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
                        flowOf(buildState(
                            assets = basicData.assets,
                            categories = basicData.categories,
                            account = basicData.account,
                            transactions = basicData.transactions,
                            platforms = basicData.platforms,
                            platformsByAsset = emptyMap(),
                            fiSummary = basicData.fiSummary,
                            nearMaturityPositions = basicData.nearMaturityPositions,
                            accountId = basicData.account?.id,
                            bondIssuers = basicData.bondIssuers,
                            bankIssuers = basicData.bankIssuers
                        ))
                    } else {
                        combine(
                            assetPlatformRepository.getPlatformsByAssets(assetIds),
                            assetMetadataRepository.getAllCompositions(),
                            assetMetadataRepository.getSectorsByAssetIds(assetIds),
                            assetMetadataRepository.getRegionDistributionsByAssetIds(assetIds)
                        ) { platformsByAsset, compositions, sectorRelations, regionDistributions ->
                            buildState(
                                assets = basicData.assets,
                                categories = basicData.categories,
                                account = basicData.account,
                                transactions = basicData.transactions,
                                platforms = basicData.platforms,
                                platformsByAsset = platformsByAsset,
                                fiSummary = basicData.fiSummary,
                                nearMaturityPositions = basicData.nearMaturityPositions,
                                accountId = basicData.account?.id,
                                compositions = compositions,
                                sectorRelations = sectorRelations,
                                regionDistributions = regionDistributions,
                                bondIssuers = basicData.bondIssuers,
                                bankIssuers = basicData.bankIssuers
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
        .combine(_selectedDistributionView) { state, view ->
            state.copy(selectedDistributionView = view)
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
        accountId: String? = null,
        compositions: List<es.aviferdev.trackfolio.domain.model.AssetComposition> = emptyList(),
        sectorRelations: List<es.aviferdev.trackfolio.domain.model.AssetSectorRelation> = emptyList(),
        regionDistributions: List<es.aviferdev.trackfolio.domain.model.AssetRegionDistribution> = emptyList(),
        bondIssuers: List<Issuer> = emptyList(),
        bankIssuers: List<Issuer> = emptyList()
    ): PortfolioUiState {
        val allSectorsList = allSectors.value
        val allRegionsList = allRegions.value
        // Agrupar movimientos por activo.
        val txByAsset: Map<String, List<AssetTransaction>> =
            transactions.groupBy { it.assetId }

        // Group fixed income by category
        val fiRows = fiSummary?.positions ?: emptyList()
        val fiByCategory = fiRows.groupBy { it.position.assetCategoryId }
        val categoryById = categories.associateBy { it.id }

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

                // Include fixed income for this category
                val fiRowsForCat = fiByCategory[categoryId].orEmpty()
                val fiInvested = fiRowsForCat.sumOf { it.position.principal }
                val fiCurrent = fiRowsForCat.sumOf { it.currentValue }
                val fiProfit = fiRowsForCat.sumOf { it.totalProfit }

                CategoryGroup(
                    category           = cat,
                    rows               = groupRows.sortedByDescending { it.position.currentValue },
                    fixedIncomeRows   = fiRowsForCat,
                    totalInvested      = invested + fiInvested,
                    totalCurrentValue  = current + fiCurrent,
                    totalUnrealizedPnL = unrealized,
                    totalRealizedPnL   = realized,
                    totalPnL           = total + fiProfit,
                    totalPnLPercent    = if (invested + fiInvested > 0.0) ((total + fiProfit) / (invested + fiInvested)) * 100.0 else 0.0
                )
            }
            .sortedWith(
                compareBy(
                    { if (it.category == null) 1 else 0 },
                    { it.sortKey },
                    { it.displayName }
                )
            )

        // Create groups for categories that have only fixed income (no stocks)
        val stockCategoryIds = grouped.keys
        val fiOnlyCategoryIds = fiByCategory.keys - stockCategoryIds
        val fiOnlyGroups = fiOnlyCategoryIds.mapNotNull { categoryId ->
            val fiRowsForCat = fiByCategory[categoryId].orEmpty()
            if (fiRowsForCat.isEmpty()) return@mapNotNull null
            val cat = categoryId?.let { categoryById[it] }
            val fiInvested = fiRowsForCat.sumOf { it.position.principal }
            val fiCurrent = fiRowsForCat.sumOf { it.currentValue }
            val fiProfit = fiRowsForCat.sumOf { it.totalProfit }
            CategoryGroup(
                category           = cat,
                rows               = emptyList(),
                fixedIncomeRows   = fiRowsForCat,
                totalInvested      = fiInvested,
                totalCurrentValue  = fiCurrent,
                totalUnrealizedPnL = 0.0,
                totalRealizedPnL   = 0.0,
                totalPnL           = fiProfit,
                totalPnLPercent    = if (fiInvested > 0.0) (fiProfit / fiInvested) * 100.0 else 0.0
            )
        }

        val allGroups = (groups + fiOnlyGroups).sortedWith(
            compareBy(
                { if (it.category == null) 1 else 0 },
                { it.sortKey },
                { it.displayName }
            )
        )

        // ── Groups by Region ─────────────────────────────────────────────────────
        val regionGroups = buildRegionGroups(openRows, fiSummary?.positions.orEmpty())

        // ── Groups by Sector ───────────────────────────────────────────────────────
        val sectorGroups = buildSectorGroups(openRows, fiSummary?.positions.orEmpty())

        val totalInvested      = allGroups.sumOf { it.totalInvested }
        val totalCurrentValue  = allGroups.sumOf { it.totalCurrentValue }
        // El P&L realizado SUMA tanto el de las posiciones abiertas (ventas
        // parciales) como el de las cerradas (vendidas por completo).
        val totalRealizedPnL   = allGroups.sumOf { it.totalRealizedPnL } +
                                 closedRows.sumOf { it.position.realizedPnL }
        val totalUnrealizedPnL = allGroups.sumOf { it.totalUnrealizedPnL }
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
            allGroups
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
                .sortedByDescending { it.percent }
        }

        // ── Distribución por composición RF/RV ─────────────────────────
        val compositionByAsset = compositions.associateBy { it.assetId }
        val compositionSlices = buildCompositionDistribution(allGroups, compositionByAsset, combinedCurrentValue)

        // ── Distribución por región ────────────────────────────────────
        val assetCurrentValues = openRows.associate { it.asset.id to it.position.currentValue }
        val regionById = allRegionsList.associateBy { it.id }
        val regionValues = mutableMapOf<String, Double>()
        val catalogedAssetIds = mutableSetOf<String>()

        for (dist in regionDistributions) {
            val assetValue = assetCurrentValues[dist.assetId] ?: continue
            val weight = dist.percent / 100.0
            regionValues[dist.regionId] = (regionValues[dist.regionId] ?: 0.0) + (assetValue * weight)
            catalogedAssetIds.add(dist.assetId)
        }

        // Activos sin región → "No catalogados"
        for ((assetId, value) in assetCurrentValues) {
            if (assetId !in catalogedAssetIds) {
                regionValues["__uncatalogued__"] = (regionValues["__uncatalogued__"] ?: 0.0) + value
            }
        }

        val regionSlices: List<CategorySlice> = if (combinedCurrentValue > 0.0 && regionValues.isNotEmpty()) {
            regionValues.map { (regionId, value) ->
                if (regionId == "__uncatalogued__") {
                    CategorySlice(
                        categoryId = null,
                        name = "No catalogados",
                        icon = "❔",
                        value = value,
                        percent = (value / combinedCurrentValue) * 100.0,
                        color = UncategorizedColor
                    )
                } else {
                    val region = regionById[regionId]
                    CategorySlice(
                        categoryId = regionId,
                        name = region?.name ?: regionId,
                        icon = "🌍",
                        value = value,
                        percent = (value / combinedCurrentValue) * 100.0,
                        color = CategoryPalette[regionValues.keys.indexOf(regionId) % CategoryPalette.size]
                    )
                }
            }.sortedByDescending { it.percent }
        } else {
            emptyList()
        }

        // ── Distribución por sector ────────────────────────────────────
        val sectorById = allSectorsList.associateBy { it.id }
        val sectorValues = mutableMapOf<String, Double>()
        val sectorsByAsset = sectorRelations.groupBy { it.assetId }
        val catalogedAssetIdsForSector = mutableSetOf<String>()

        for ((assetId, relations) in sectorsByAsset) {
            val assetValue = assetCurrentValues[assetId] ?: continue
            val sectorCount = relations.size
            if (sectorCount > 0) {
                val valuePerSector = assetValue / sectorCount
                for (rel in relations) {
                    sectorValues[rel.sectorId] = (sectorValues[rel.sectorId] ?: 0.0) + valuePerSector
                }
                catalogedAssetIdsForSector.add(assetId)
            }
        }

        // Activos sin sector → "No catalogados"
        for ((assetId, value) in assetCurrentValues) {
            if (assetId !in catalogedAssetIdsForSector) {
                sectorValues["__uncatalogued__"] = (sectorValues["__uncatalogued__"] ?: 0.0) + value
            }
        }

        val sectorSlices: List<CategorySlice> = if (combinedCurrentValue > 0.0 && sectorValues.isNotEmpty()) {
            sectorValues.map { (sectorId, value) ->
                if (sectorId == "__uncatalogued__") {
                    CategorySlice(
                        categoryId = null,
                        name = "No catalogados",
                        icon = "❔",
                        value = value,
                        percent = (value / combinedCurrentValue) * 100.0,
                        color = UncategorizedColor
                    )
                } else {
                    val sector = sectorById[sectorId]
                    CategorySlice(
                        categoryId = sectorId,
                        name = sector?.name ?: sectorId,
                        icon = sector?.icon ?: "📊",
                        value = value,
                        percent = (value / combinedCurrentValue) * 100.0,
                        color = CategoryPalette[sectorValues.keys.indexOf(sectorId) % CategoryPalette.size]
                    )
                }
            }.sortedByDescending { it.percent }
        } else {
            emptyList()
        }

        return PortfolioUiState(
            groups             = allGroups,
            regionGroups       = regionGroups,
            sectorGroups       = sectorGroups,
            closedPositions    = closedRows.sortedByDescending { it.position.realizedPnL },
            closedFixedIncomePositions = fiSummary?.closedPositions ?: emptyList(),
            distribution       = distribution,
            compositionDistribution = compositionSlices,
            regionDistribution       = regionSlices,
            sectorDistribution       = sectorSlices,
            selectedDistributionView  = _selectedDistributionView.value,
            totalInvested      = totalInvested,
            totalCurrentValue  = totalCurrentValue,
            totalRealizedPnL   = totalRealizedPnL,
            totalUnrealizedPnL = totalUnrealizedPnL,
            totalPnL           = totalPnL,
            totalPnLPercent    = if (totalInvested > 0.0) (totalPnL / totalInvested) * 100.0 else 0.0,
            openPositionsCount = openRows.size,
            allAssets          = assets,
            platforms          = platforms,
            platformsByAsset  = platformsByAsset,
            bondIssuers       = bondIssuers,
            bankIssuers       = bankIssuers,
            isLoading          = false,
            fixedIncomeSummary = fiSummary,
            nearMaturityPositions = nearMaturityPositions,
            combinedInvested   = combinedInvested,
            combinedCurrentValue = combinedCurrentValue,
            combinedPnL        = combinedPnL,
            combinedPnLPercent = combinedPnLPercent,
            combinedRealizedPnL = totalRealizedPnL + (fiSummary?.totalCollectedInterest ?: 0.0),
            combinedUnrealizedPnL = totalUnrealizedPnL + (fiSummary?.totalAccruedInterest ?: 0.0),
            currentAccountId = accountId,
            allSectors = allSectorsList,
            allRegions = allRegionsList
        )
    }

    private fun colorForGroup(group: CategoryGroup, fallbackIndex: Int): Color {
        val cat = group.category ?: return UncategorizedColor
        val idx = if (cat.sortOrder >= 0) cat.sortOrder else fallbackIndex
        return CategoryPalette[idx % CategoryPalette.size]
    }

    private fun buildCompositionDistribution(
        groups: List<CategoryGroup>,
        compositionByAsset: Map<String, es.aviferdev.trackfolio.domain.model.AssetComposition>,
        totalValue: Double
    ): List<CategorySlice> {
        if (totalValue <= 0.0) return emptyList()

        // Calcular valor de renta fija y renta variable basado en AssetComposition
        var rfValue = 0.0
        var rvValue = 0.0

        // Incluir renta fija de FixedIncomeSummary
        val fiSummary = _sheetState.value.fixedIncomeSummary
        rfValue += fiSummary?.totalCurrentValue ?: 0.0

        // Calcular RV y RF restante de los grupos de activos
        groups.forEach { group ->
            group.rows.forEach { assetRow ->
                val composition = compositionByAsset[assetRow.asset.id]
                val assetValue = assetRow.position.currentValue
                if (composition != null && composition.fixedIncomePercent > 0) {
                    // Este activo tiene composición mixta
                    val rfPart = assetValue * (composition.fixedIncomePercent / 100.0)
                    val rvPart = assetValue - rfPart
                    rfValue += rfPart
                    rvValue += rvPart
                } else {
                    // Por defecto es renta variable (bolsa)
                    rvValue += assetValue
                }
            }
        }

        val slices = mutableListOf<CategorySlice>()
        if (rfValue > 0) {
            slices.add(CategorySlice(
                categoryId = "rf",
                name = "Renta fija",
                icon = "🏦",
                value = rfValue,
                percent = (rfValue / totalValue) * 100.0,
                color = WarnAmber
            ))
        }
        if (rvValue > 0) {
            slices.add(CategorySlice(
                categoryId = "rv",
                name = "Renta variable",
                icon = "📈",
                value = rvValue,
                percent = (rvValue / totalValue) * 100.0,
                color = PositiveGreen
            ))
        }

        return slices.sortedByDescending { it.percent }
    }

    private fun buildRegionGroups(
        openRows: List<AssetRow>,
        fiPositions: List<FixedIncomeRow>
    ): List<CategoryGroup> {
        // Agrupar renta fija por región
        val fiByRegion = fiPositions.groupBy { row ->
            row.position.region
        }

        // Los activos de bolsa sin región van a "No catalogados"
        val assetsWithoutRegion = openRows.filter { row ->
            // Verificar si tiene metadata de región
            val hasRegionMetadata = false // Por ahora los activos no tienen región asignada
            !hasRegionMetadata
        }

        // Obtener todas las regiones de FI + "No catalogados" para activos
        val allRegions = mutableSetOf<String?>()
        allRegions.addAll(fiByRegion.keys)
        if (assetsWithoutRegion.isNotEmpty()) {
            allRegions.add("No catalogados")
        }

        return allRegions.map { region ->
            val fiRows = fiByRegion[region].orEmpty()
            val assetRows = if (region == null) assetsWithoutRegion else emptyList()

            val invested = assetRows.sumOf { it.position.totalInvestedRemaining }
            val current = assetRows.sumOf { it.position.currentValue }
            val realized = assetRows.sumOf { it.position.realizedPnL }
            val unrealized = assetRows.sumOf { it.position.unrealizedPnL }

            val fiInvested = fiRows.sumOf { it.position.principal }
            val fiCurrent = fiRows.sumOf { it.currentValue }
            val fiProfit = fiRows.sumOf { it.totalProfit }

            val displayName = region ?: "No catalogados"

            CategoryGroup(
                category = null,
                customName = displayName,
                rows = assetRows.sortedByDescending { it.position.currentValue },
                fixedIncomeRows = fiRows,
                totalInvested = invested + fiInvested,
                totalCurrentValue = current + fiCurrent,
                totalUnrealizedPnL = unrealized,
                totalRealizedPnL = realized,
                totalPnL = (realized + unrealized) + fiProfit,
                totalPnLPercent = if (invested + fiInvested > 0.0) {
                    ((realized + unrealized + fiProfit) / (invested + fiInvested)) * 100.0
                } else 0.0
            )
        }.sortedByDescending { it.totalCurrentValue }
    }

    private fun buildSectorGroups(
        openRows: List<AssetRow>,
        fiPositions: List<FixedIncomeRow>
    ): List<CategoryGroup> {
        // Agrupar renta fija por sector
        val fiBySector = fiPositions.groupBy { row ->
            row.position.sector
        }

        // Los activos de bolsa sin sector van a "No catalogados"
        val assetsWithoutSector = openRows.filter { row ->
            // Verificar si tiene metadata de sector
            val hasSectorMetadata = false // Por ahora los activos no tienen sector asignado
            !hasSectorMetadata
        }

        // Obtener todos los sectores de FI + "No catalogados" para activos
        val allSectors = mutableSetOf<String?>()
        allSectors.addAll(fiBySector.keys)
        if (assetsWithoutSector.isNotEmpty()) {
            allSectors.add("No catalogados")
        }

        return allSectors.map { sector ->
            val fiRows = fiBySector[sector].orEmpty()
            val assetRows = if (sector == null) assetsWithoutSector else emptyList()

            val invested = assetRows.sumOf { it.position.totalInvestedRemaining }
            val current = assetRows.sumOf { it.position.currentValue }
            val realized = assetRows.sumOf { it.position.realizedPnL }
            val unrealized = assetRows.sumOf { it.position.unrealizedPnL }

            val fiInvested = fiRows.sumOf { it.position.principal }
            val fiCurrent = fiRows.sumOf { it.currentValue }
            val fiProfit = fiRows.sumOf { it.totalProfit }

            val displayName = sector ?: "No catalogados"

            CategoryGroup(
                category = null,
                customName = displayName,
                rows = assetRows.sortedByDescending { it.position.currentValue },
                fixedIncomeRows = fiRows,
                totalInvested = invested + fiInvested,
                totalCurrentValue = current + fiCurrent,
                totalUnrealizedPnL = unrealized,
                totalRealizedPnL = realized,
                totalPnL = (realized + unrealized) + fiProfit,
                totalPnLPercent = if (invested + fiInvested > 0.0) {
                    ((realized + unrealized + fiProfit) / (invested + fiInvested)) * 100.0
                } else 0.0
            )
        }.sortedByDescending { it.totalCurrentValue }
    }

    // ── Sheet rápido de actualización de precio ──────────────────────────────
    fun openUpdatePriceSheet(asset: Asset) {
        _sheetState.value = _sheetState.value.copy(showUpdatePriceSheet = true, pricingAsset = asset)
    }

    fun closeUpdatePriceSheet() {
        _sheetState.value = _sheetState.value.copy(showUpdatePriceSheet = false, pricingAsset = null)
    }

    fun selectDistributionView(view: DistributionView) {
        _selectedDistributionView.value = view
    }

    fun refreshCurrentPrice(asset: Asset, newPrice: Double) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            updateAssetCurrentPrice(asset.id, newPrice, now, asset.assetCategoryId)
                .onSuccess {
                    getPortfolioValueHistory.triggerRefresh()
                    closeUpdatePriceSheet()
                }
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
            // Validar que el activo no sea de Renta Fija
            val asset = portfolioState.value.allAssets.find { it.id == assetId }
            if (asset != null && AssetCategoryType.isFixedIncome(asset.assetCategoryId)) {
                _sheetState.value = _sheetState.value.copy(error = "Los activos de Renta Fija no admiten movimientos de compra/venta")
                return@launch
            }

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
                        if (type == AssetTransactionType.BUY) {
                            assetPriceHistoryRepository.insert(
                                AssetPriceHistory(
                                    id         = uuid4().toString(),
                                    assetId    = assetId,
                                    price      = pricePerUnit,
                                    recordedAt = date
                                )
                            ).onFailure { err ->
                                _sheetState.value = _sheetState.value.copy(error = "Error al registrar precio histórico: ${err.message}")
                            }
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

    // ── Renta fija: registrar cupón ──────────────────────────────────────
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
                ?.onFailure { _sheetState.value = _sheetState.value.copy(error = it.message) }
                ?: run { _sheetState.value = _sheetState.value.copy(error = "Error al registrar cupón") }
        }
    }

    fun saveBondIssuer(name: String, icon: String, type: IssuerType) {
        viewModelScope.launch {
            val accountId = session.selectedAccountId.value
            if (accountId == null) {
                _sheetState.value = _sheetState.value.copy(error = "No hay cuenta seleccionada")
                return@launch
            }
            val now = Clock.System.now().toEpochMilliseconds()
            val prefix = when (type) {
                IssuerType.BANK -> "bk"
                else -> "bi"
            }
            val issuer = Issuer(
                id = "${prefix}_$now",
                accountId = accountId,
                name = name,
                type = type,
                icon = icon,
                archived = false,
                createdAt = now
            )
            saveBondIssuer.invoke(issuer)
                .onFailure { _sheetState.value = _sheetState.value.copy(error = it.message) }
        }
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val day1 = timestamp1 / (24 * 60 * 60 * 1000)
        val day2 = timestamp2 / (24 * 60 * 60 * 1000)
        return day1 == day2
    }
}
