package es.aviferdev.n3to.ui.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.account.AccountViewModel
import es.aviferdev.n3to.ui.common.DeltaIndicator
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.common.LineChartCard
import es.aviferdev.n3to.ui.common.chart.TimeRange
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.component.TimeRangeChipRow
import es.aviferdev.n3to.ui.common.button.IconButtonApp
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.fixedincome.CreateFixedIncomeBottomSheet
import es.aviferdev.n3to.ui.fixedincome.FixedIncomePositionCard
import es.aviferdev.n3to.ui.fixedincome.RegisterCouponBottomSheet
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.BorderGray
import es.aviferdev.n3to.ui.theme.BorderGray2
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceElevated
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.WarnAmber
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatPercent
import es.aviferdev.n3to.ui.theme.maskAmount
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.PortfolioValuePoint
import es.aviferdev.n3to.domain.portfolio.AssetPosition
import kotlinx.datetime.Clock
import es.aviferdev.n3to.ui.theme.N3toTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onAssetClick: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onFixedIncomeClick: (String) -> Unit = {},
    viewModel: PortfolioViewModel = koinViewModel(),
    catalogViewModel: AssetCatalogViewModel = koinViewModel(),
    platformViewModel: PlatformViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel()
) {
    val state by viewModel.portfolioState.collectAsState()
    val catalogState by catalogViewModel.uiState.collectAsState()
    val platformState by platformViewModel.uiState.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val valueHistory by viewModel.portfolioValueHistory.collectAsState()
    val balancesHidden = LocalBalanceHidden.current

    accountViewModel.selectAccount()

    PortfolioContent(
        state = state,
        valueHistory = valueHistory,
        balancesHidden = balancesHidden,
        onAssetClick = onAssetClick,
        onNavigateToSettings = onNavigateToSettings,
        onFixedIncomeClick = onFixedIncomeClick,
        onSelectDistributionView = { viewModel.selectDistributionView(it) },
        onOpenAddTransactionSheet = { viewModel.openAddTransactionSheet() },
        onOpenCreateFixedIncomeSheet = { viewModel.openCreateFixedIncomeSheet() },
        onOpenUpdatePriceSheet = { asset -> viewModel.openUpdatePriceSheet(asset) },
        onShowRegisterCouponSheet = { position -> viewModel.showRegisterCouponSheet(position) }
    )

    if (state.showAddTxSheet) {
        AddEditAssetTransactionBottomSheet(
            transaction = null, fixedAsset = null,
            allAssets = state.allAssets, platforms = state.platforms,
            platformsByAsset = state.platformsByAsset, categories = availableCategories,
            assetTransactions = emptyList(),  buyOnly = true,
            onSave = { assetId, type, qty, price, date, platformId, feeNote, notes ->
                viewModel.addTransaction(assetId, type, qty, price, date, platformId, feeNote, notes)
            },
            onDismiss = { viewModel.closeAddTransactionSheet() }
        )
    }
    if (state.showCreateFixedIncomeSheet && state.currentAccountId != null) {
        CreateFixedIncomeBottomSheet(
            platforms = state.platforms,
            categories = availableCategories,
            bondIssuers = state.bondIssuers,
            bankIssuers = state.bankIssuers,
            accountId = state.currentAccountId!!,
            onSave = { position, event -> viewModel.saveFixedIncomePosition(position, event) },
            onSaveIssuer = { name, icon, type -> viewModel.saveBondIssuer(name, icon, type) },
            onDismiss = { viewModel.closeCreateFixedIncomeSheet() }
        )
    }
    if (state.showRegisterCouponSheet && state.selectedPositionForCoupon != null) {
        RegisterCouponBottomSheet(
            positionName = state.selectedPositionForCoupon!!.name,
            onSave = { event -> viewModel.registerCoupon(event) },
            onDismiss = { viewModel.hideRegisterCouponSheet() }
        )
    }
    if (state.showUpdatePriceSheet && state.pricingAsset != null) {
        UpdateCurrentPriceSheet(
            asset = state.pricingAsset!!, 
            onConfirm = { newPrice -> viewModel.refreshCurrentPrice(state.pricingAsset!!, newPrice) },
            onDismiss = { viewModel.closeUpdatePriceSheet() }
        )
    }
    if (catalogState.showAddSheet) {
        AddEditAssetBottomSheet(
            asset = null,
            categories = availableCategories,
            
            allPlatforms = state.platforms,
            allSectors = state.allSectors,
            linkedSectorIds = emptySet(),
            allRegions = state.allRegions,
            linkedRegionPercents = emptyMap(),
            onSave = { ticker, name, notes, categoryId, currentPrice, platformIds, _, fixedPct, sectorIds, regionPercents ->
                catalogViewModel.addAsset(ticker, name, notes, categoryId, currentPrice, platformIds, fixedPct, sectorIds, regionPercents)
            },
            onDismiss = { catalogViewModel.closeAddSheet() }
        )
    }
    catalogState.editing?.let { editing ->
        AddEditAssetBottomSheet(
            asset = editing,
            categories = availableCategories,
            
            allPlatforms = state.platforms,
            linkedPlatformIds = catalogState.editingPlatformIds,
            allSectors = state.allSectors,
            linkedSectorIds = catalogState.editingSectorIds,
            allRegions = state.allRegions,
            linkedRegionPercents = catalogState.editingRegionPercents,
            linkedFixedIncomePercent = catalogState.editingFixedIncomePercent,
            onSave = { ticker, name, notes, categoryId, currentPrice, platformIds, _, fixedPct, sectorIds, regionPercents ->
                catalogViewModel.editAsset(editing, ticker, name, notes, categoryId, currentPrice, platformIds, fixedPct, sectorIds, regionPercents)
            },
            onDismiss = { catalogViewModel.closeEditSheet() }
        )
    }
    if (platformState.showAddSheet) {
        AddEditPlatformSheet(
            initial = null,
            onSave = { name, icon, notes -> platformViewModel.addPlatform(name, icon, notes) },
            onDismiss = { platformViewModel.closeAddSheet() }
        )
    }
    for (msg in listOfNotNull(state.error, catalogState.error, platformState.error)) {
        val clearFn: () -> Unit = when (msg) {
            state.error -> { { viewModel.clearError() } }
            catalogState.error -> { { catalogViewModel.clearError() } }
            else -> { { platformViewModel.clearError() } }
        }
        AlertDialog(
            onDismissRequest = clearFn,
            containerColor = SurfaceWhite,
            title = { Text("Error", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text(msg, fontSize = 13.sp, color = TextSecondary) },
            confirmButton = { TextButton(onClick = clearFn) { Text("Aceptar", color = PrimaryDark) } },
            shape = RoundedCornerShape(16.dp)
        )
        break
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioContent(
    state: PortfolioUiState,
    valueHistory: List<PortfolioValuePoint>,
    balancesHidden: Boolean,
    onAssetClick: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onFixedIncomeClick: (String) -> Unit,
    onSelectDistributionView: (DistributionView) -> Unit,
    onOpenAddTransactionSheet: () -> Unit,
    onOpenCreateFixedIncomeSheet: () -> Unit,
    onOpenUpdatePriceSheet: (Asset) -> Unit,
    onShowRegisterCouponSheet: (FixedIncomePosition) -> Unit,
    modifier: Modifier = Modifier
) {
    var closedExpanded by remember { mutableStateOf(false) }
    var fabMenuOpen by remember { mutableStateOf(false) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.ALL_TIME) }

    val nowMillis = remember { Clock.System.now().toEpochMilliseconds() }
    val filteredHistory = remember(valueHistory, selectedTimeRange) {
        if (selectedTimeRange == TimeRange.ALL_TIME) {
            valueHistory
        } else {
            val cutoff = nowMillis - selectedTimeRange.windowDays * 86_400_000L
            valueHistory.filter { it.date >= cutoff }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(Modifier.fillMaxSize()) {
            TopBarApp(
                title = "Portfolio",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Ajustes de portfolio", tint = TextSecondary)
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
            item {
                PortfolioSummaryCard(
                    totalInvested = state.totalInvested,
                    totalCurrentValue = state.totalCurrentValue,
                    totalPnL = state.totalPnL,
                    totalPnLPercent = state.totalPnLPercent,
                    totalRealizedPnL = state.totalRealizedPnL,
                    totalUnrealizedPnL = state.totalUnrealizedPnL,
                    positionsCount = state.openPositionsCount,
                    
                    balancesHidden = balancesHidden,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (valueHistory.isNotEmpty()) {
                item {
                    Column {
                        TimeRangeChipRow(
                            selected = selectedTimeRange,
                            onSelect = { selectedTimeRange = it },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                item {
                    LineChartCard(
                        title = "Evolución del valor",
                        subtitle = "Valor mensual del portfolio",
                        points = filteredHistory.map { it.date to it.value },
                        lineColor = PrimaryDark,
                        balancesHidden = balancesHidden,
                        rotateXLabels = true,
                        timeRangeLabel = if (selectedTimeRange != TimeRange.ALL_TIME) {
                            when (selectedTimeRange) {
                                TimeRange.LAST_MONTH -> "Último mes"
                                TimeRange.LAST_YEAR -> "Último año"
                                else -> null
                            }
                        } else null,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            val hasDistribution = state.distribution.isNotEmpty()
                    || state.compositionDistribution.isNotEmpty()
                    || state.regionDistribution.isNotEmpty()
                    || state.sectorDistribution.isNotEmpty()

            if (hasDistribution) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DistributionView.entries.forEach { view ->
                            val selected = state.selectedDistributionView == view
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) PrimaryAlpha else Color.Transparent)
                                    .border(1.dp, if (selected) PrimaryDark else BorderGray2, RoundedCornerShape(18.dp))
                                    .clickable { onSelectDistributionView(view) }
                                    .padding(horizontal = 11.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    view.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) PrimaryDark else TextTertiary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    val currentDist = when (state.selectedDistributionView) {
                        DistributionView.CATEGORY -> state.distribution
                        DistributionView.COMPOSITION -> state.compositionDistribution
                        DistributionView.REGION -> state.regionDistribution
                        DistributionView.SECTOR -> state.sectorDistribution
                    }
                    PortfolioDistributionCard(
                        slices = currentDist,
                        totalCurrentValue = state.combinedCurrentValue,
                        
                        balancesHidden = balancesHidden,
                        selectedView = state.selectedDistributionView,
                        fixedIncomePercent = state.fixedIncomeSummary?.let { fi ->
                            if (state.combinedCurrentValue > 0) (fi.totalCurrentValue / state.combinedCurrentValue) * 100 else 0.0
                        } ?: 0.0,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            when {
                state.isLoading -> item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                }

                state.groups.isEmpty() && state.closedPositions.isEmpty() -> item {
                    EmptyStateView(
                        icon = "📈",
                        title = "Sin posiciones",
                        subtitle = "Pulsa + para registrar\ntu primera inversión"
                    )
                }

                else -> {
                    state.groups.forEach { group ->
                        item(key = "hdr_${group.category?.id ?: "none"}") {
                            CategoryGroupHeader(
                                group = group,
                                
                                balancesHidden = balancesHidden
                            )
                        }
                        items(group.rows, key = { "open_${it.asset.id}" }) { row ->
                            AssetCard(
                                row = row,
                                
                                balancesHidden = balancesHidden,
                                onClick = { onAssetClick(row.asset.id) },
                                onUpdatePrice = { onOpenUpdatePriceSheet(row.asset) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                            )
                        }
                        if (group.fixedIncomeRows.isNotEmpty()) {
                            item(key = "fi_hdr_${group.category?.id ?: "none"}") {
                                FixedIncomeSectionHeader(
                                    count = group.fixedIncomeRows.size,
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(group.fixedIncomeRows, key = { "fi_${it.position.id}" }) { fiRow ->
                                FixedIncomePositionCard(
                                    row = fiRow,
                                    
                                    balancesHidden = balancesHidden,
                                    onClick = { onFixedIncomeClick(fiRow.position.id) },
                                    onRegisterCoupon = if (fiRow.position.hasPeriodicCoupons) {
                                        { onShowRegisterCouponSheet(fiRow.position) }
                                    } else null,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    val totalClosedCount = state.closedPositions.size + state.closedFixedIncomePositions.size
                    if (totalClosedCount > 0) {
                        item(key = "closed_hdr") {
                            ClosedPositionsHeader(
                                count = totalClosedCount,
                                expanded = closedExpanded,
                                onToggle = { closedExpanded = !closedExpanded }
                            )
                        }
                        item(key = "closed_list") {
                            AnimatedVisibility(
                                visible = closedExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    state.closedPositions.forEach { row ->
                                        ClosedAssetCard(
                                            row = row,
                                            
                                            balancesHidden = balancesHidden,
                                            onClick = { onAssetClick(row.asset.id) },
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                                        )
                                    }
                                    state.closedFixedIncomePositions.forEach { fiRow ->
                                        ClosedFixedIncomeCard(
                                            row = fiRow,
                                            
                                            balancesHidden = balancesHidden,
                                            onClick = { onFixedIncomeClick(fiRow.position.id) },
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 136.dp)
        ) {
            FloatingActionButton(
                onClick = { fabMenuOpen = true },
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = PrimaryDark,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = Color.White)
            }
            DropdownMenu(
                expanded = fabMenuOpen,
                onDismissRequest = { fabMenuOpen = false },
                containerColor = SurfaceWhite
            ) {
                DropdownMenuItem(
                    text = { Text("Nueva compra", color = TextPrimary, fontSize = 14.sp) },
                    leadingIcon = { Text("↗", fontSize = 15.sp) },
                    onClick = { fabMenuOpen = false; onOpenAddTransactionSheet() }
                )
                DropdownMenuItem(
                    text = { Text("Nuevo bono/depósito", color = TextPrimary, fontSize = 14.sp) },
                    leadingIcon = { Icon("🏦".toMaterialIcon(), contentDescription = null, modifier = Modifier.size(18.dp)) },
                    onClick = { fabMenuOpen = false; onOpenCreateFixedIncomeSheet() }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PortfolioContentPreview() {
    N3toTheme {
        PortfolioContent(
            state = PortfolioUiState(
                totalInvested = 10000.0,
                totalCurrentValue = 12500.0,
                totalPnL = 2500.0,
                totalPnLPercent = 25.0,
                totalRealizedPnL = 500.0,
                totalUnrealizedPnL = 2000.0,
                openPositionsCount = 5,
                groups = listOf(
                    CategoryGroup(
                        category = AssetCategory(id = "cat1", name = "Acciones", icon = "📈", sortOrder = 0, createdAt = 0L),
                        rows = listOf(
                            AssetRow(
                                asset = Asset(id = "a1", accountId = "acc1", ticker = "AAPL", name = "Apple Inc.", notes = null, createdAt = 0L, assetCategoryId = "cat1", currentPrice = 150.0),
                                position = AssetPosition(netQuantity = 10.0, averageCostOfRemaining = 100.0, totalInvestedRemaining = 1000.0, realizedPnL = 0.0, currentValue = 1500.0, unrealizedPnL = 500.0, unrealizedPnLPercent = 50.0, totalPnL = 500.0, totalPnLPercent = 50.0, hasCurrentPrice = true)
                            ),
                            AssetRow(
                                asset = Asset(id = "a2", accountId = "acc1", ticker = "MSFT", name = "Microsoft Corp.", notes = null, createdAt = 0L, assetCategoryId = "cat1", currentPrice = 250.0),
                                position = AssetPosition(netQuantity = 5.0, averageCostOfRemaining = 150.0, totalInvestedRemaining = 750.0, realizedPnL = 0.0, currentValue = 1250.0, unrealizedPnL = 500.0, unrealizedPnLPercent = 66.67, totalPnL = 500.0, totalPnLPercent = 66.67, hasCurrentPrice = true)
                            )
                        ),
                        totalInvested = 1750.0,
                        totalCurrentValue = 2750.0,
                        totalUnrealizedPnL = 1000.0,
                        totalRealizedPnL = 0.0,
                        totalPnL = 1000.0,
                        totalPnLPercent = 57.14
                    )
                ),
                distribution = listOf(
                    CategorySlice(categoryId = "cat1", name = "Acciones", icon = "📈", value = 12500.0, percent = 100.0, color = PrimaryDark)
                ),
                isLoading = false
            ),
            valueHistory = listOf(
                PortfolioValuePoint(date = 1704067200000L, value = 10000.0),
                PortfolioValuePoint(date = 1706745600000L, value = 11000.0),
                PortfolioValuePoint(date = 1709251200000L, value = 12500.0)
            ),
            balancesHidden = false,
            onAssetClick = {},
            onNavigateToSettings = {},
            onFixedIncomeClick = {},
            onSelectDistributionView = {},
            onOpenAddTransactionSheet = {},
            onOpenCreateFixedIncomeSheet = {},
            onOpenUpdatePriceSheet = {},
            onShowRegisterCouponSheet = {}
        )
    }
}

// Componentes extraídos a archivos propios:
// PortfolioSummaryCard → PortfolioSummaryCard.kt
// CategoryGroupHeader → CategoryGroupHeader.kt
// AssetCard → AssetCard.kt
// ClosedAssetCard → ClosedAssetCard.kt
// ClosedFixedIncomeCard → ClosedFixedIncomeCard.kt
// ClosedPositionsHeader → ClosedPositionsHeader.kt
// FixedIncomeSectionHeader → FixedIncomeSectionHeader.kt
