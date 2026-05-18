package es.aviferdev.n3to.ui.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import es.aviferdev.n3to.ui.common.LineChartWithTimeRange
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.component.IconActionButton
import es.aviferdev.n3to.ui.common.component.NavyTab
import es.aviferdev.n3to.ui.fixedincome.EditFixedIncomeBottomSheet
import es.aviferdev.n3to.ui.common.button.IconButtonApp
import es.aviferdev.n3to.ui.fixedincome.CreateFixedIncomeBottomSheet
import es.aviferdev.n3to.ui.fixedincome.FixedIncomePositionCard
import es.aviferdev.n3to.ui.fixedincome.RegisterCouponBottomSheet
import es.aviferdev.n3to.ui.theme.BorderGray
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavyDeep
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.PrimaryDark
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
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.model.PortfolioValuePoint
import es.aviferdev.n3to.domain.portfolio.AssetPosition
import es.aviferdev.n3to.ui.theme.N3toTheme
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.error_asset_not_found
import n3to.composeapp.generated.resources.error_coupon_register
import n3to.composeapp.generated.resources.error_fi_create_position
import n3to.composeapp.generated.resources.error_fi_no_buy_sell
import n3to.composeapp.generated.resources.error_account_required
import n3to.composeapp.generated.resources.error_asset_already_exists
import n3to.composeapp.generated.resources.error_asset_ticker_required
import n3to.composeapp.generated.resources.error_cannot_archive_with_open_positions_qty
import n3to.composeapp.generated.resources.error_no_account_selected
import n3to.composeapp.generated.resources.error_platform_already_exists
import n3to.composeapp.generated.resources.portfolio_empty_subtitle
import n3to.composeapp.generated.resources.portfolio_empty_title
import n3to.composeapp.generated.resources.portfolio_evolution_title
import n3to.composeapp.generated.resources.portfolio_monthly_value
import n3to.composeapp.generated.resources.portfolio_new_bond
import n3to.composeapp.generated.resources.portfolio_new_purchase
import n3to.composeapp.generated.resources.portfolio_settings_cd
import n3to.composeapp.generated.resources.portfolio_title
import org.jetbrains.compose.resources.stringResource
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
    val portfolios by viewModel.portfolios.collectAsState()
    val selectedPortfolioId by viewModel.selectedPortfolioId.collectAsState()
    val showAddPortfolioSheet by viewModel.showAddPortfolioSheet.collectAsState()
    val balancesHidden = LocalBalanceHidden.current

    accountViewModel.selectAccount()

    PortfolioContent(
        portfolios = portfolios,
        selectedPortfolioId = selectedPortfolioId,
        onSelectPortfolio = { viewModel.selectPortfolio(it) },
        onAddPortfolio = { viewModel.openAddPortfolioSheet() },
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
            portfolios = portfolios,
            onSave = { ticker, name, notes, categoryId, currentPrice, platformIds, _, fixedPct, sectorIds, regionPercents, portfolioId ->
                catalogViewModel.addAsset(ticker, name, notes, categoryId, currentPrice, platformIds, fixedPct, sectorIds, regionPercents, portfolioId)
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
            portfolios = portfolios,
            selectedPortfolioId = editing.portfolioId,
            onSave = { ticker, name, notes, categoryId, currentPrice, platformIds, _, fixedPct, sectorIds, regionPercents, portfolioId ->
                catalogViewModel.editAsset(editing, ticker, name, notes, categoryId, currentPrice, platformIds, fixedPct, sectorIds, regionPercents, portfolioId)
            },
            onDismiss = { catalogViewModel.closeEditSheet() }
        )
    }
    if (showAddPortfolioSheet) {
        AddEditPortfolioBottomSheet(
            existing = null,
            onSave = { name, desc -> viewModel.addPortfolio(name, desc) },
            onDismiss = { viewModel.closeAddPortfolioSheet() }
        )
    }
    if (platformState.showAddSheet) {
        AddEditPlatformSheet(
            initial = null,
            onSave = { name, icon, notes -> platformViewModel.addPlatform(name, icon, notes) },
            onDismiss = { platformViewModel.closeAddSheet() }
        )
    }
    val stateError = state.error
    val catalogError = catalogState.error
    val platformError = platformState.error
    if (stateError != null || catalogError != null || platformError != null) {
        val errorMsg: String = when {
            stateError != null -> when (stateError) {
                is es.aviferdev.n3to.ui.portfolio.PortfolioSheetError.FiNoBuySell -> stringResource(Res.string.error_fi_no_buy_sell)
                is es.aviferdev.n3to.ui.portfolio.PortfolioSheetError.PriceHistorySave -> stateError.message
                is es.aviferdev.n3to.ui.portfolio.PortfolioSheetError.AssetNotFound -> stringResource(Res.string.error_asset_not_found)
                is es.aviferdev.n3to.ui.portfolio.PortfolioSheetError.FiCreatePosition -> stringResource(Res.string.error_fi_create_position)
                is es.aviferdev.n3to.ui.portfolio.PortfolioSheetError.CouponRegister -> stringResource(Res.string.error_coupon_register)
                is es.aviferdev.n3to.ui.portfolio.PortfolioSheetError.NoAccountSelected -> stringResource(Res.string.error_no_account_selected)
                is es.aviferdev.n3to.ui.portfolio.PortfolioSheetError.Unknown -> stateError.message ?: stringResource(Res.string.common_error)
                else -> stringResource(Res.string.common_error)
            }
            catalogError != null -> when (catalogError) {
                is es.aviferdev.n3to.ui.portfolio.CatalogError.AccountRequired -> stringResource(Res.string.error_account_required)
                is es.aviferdev.n3to.ui.portfolio.CatalogError.TickerAndNameRequired -> stringResource(Res.string.error_asset_ticker_required)
                is es.aviferdev.n3to.ui.portfolio.CatalogError.AssetAlreadyExists -> stringResource(Res.string.error_asset_already_exists, catalogError.ticker)
                is es.aviferdev.n3to.ui.portfolio.CatalogError.CannotArchiveWithOpenPositions -> stringResource(Res.string.error_cannot_archive_with_open_positions_qty, catalogError.ticker, catalogError.qty)
                is es.aviferdev.n3to.ui.portfolio.CatalogError.Unknown -> catalogError.message ?: stringResource(Res.string.common_error)
            }
            platformError != null -> when (platformError) {
                is es.aviferdev.n3to.ui.portfolio.PlatformError.AlreadyExists -> stringResource(Res.string.error_platform_already_exists)
                is es.aviferdev.n3to.ui.portfolio.PlatformError.Unknown -> platformError.message ?: stringResource(Res.string.common_error)
            }
            else -> stringResource(Res.string.common_error)
        }
        val clearFn: () -> Unit = when {
            stateError != null -> { { viewModel.clearError() } }
            catalogError != null -> { { catalogViewModel.clearError() } }
            else -> { { platformViewModel.clearError() } }
        }
        AlertDialog(
            onDismissRequest = clearFn,
            containerColor = SurfaceWhite,
            title = { Text(stringResource(Res.string.common_error), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text(errorMsg, fontSize = 13.sp, color = TextSecondary) },
            confirmButton = { TextButton(onClick = clearFn) { Text(stringResource(Res.string.common_accept), color = PrimaryDark) } },
            shape = RoundedCornerShape(16.dp)
        )
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
    portfolios: List<Portfolio> = emptyList(),
    selectedPortfolioId: String? = null,
    onSelectPortfolio: (String?) -> Unit = {},
    onAddPortfolio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var closedExpanded by remember { mutableStateOf(false) }
    var fabMenuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // ── Cabecera estilo Home ────────────────────────────────────────────
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.portfolio_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp
                )
                IconActionButton(
                    onClick = onNavigateToSettings,
                    icon = Icons.Outlined.Settings,
                    iconTint = TextSecondary,
                    label = stringResource(Res.string.portfolio_settings_cd)
                )
            }

            PortfolioSelectorBar(
                portfolios = portfolios,
                selectedPortfolioId = selectedPortfolioId,
                onSelectPortfolio = onSelectPortfolio,
                onAddPortfolio = onAddPortfolio
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

            item {
                CompoundEffectCard(
                    compoundEffect = state.compoundEffect,
                    balancesHidden = balancesHidden,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (valueHistory.isNotEmpty()) {
                item {
                    LineChartWithTimeRange(
                        title = stringResource(Res.string.portfolio_evolution_title),
                        subtitle = stringResource(Res.string.portfolio_monthly_value),
                        points = valueHistory.map { it.date to it.value },
                        lineColor = CyanAccent,
                        balancesHidden = balancesHidden,
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
                        viewSelector = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                DistributionView.entries.forEach { view ->
                                    NavyTab(
                                        label = view.displayName,
                                        selected = state.selectedDistributionView == view,
                                        onClick = { onSelectDistributionView(view) }
                                    )
                                }
                            }
                        },
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
                        title = stringResource(Res.string.portfolio_empty_title),
                        subtitle = stringResource(Res.string.portfolio_empty_subtitle)
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
                containerColor = NavySurface,
                contentColor = CyanAccent,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = CyanAccent)
            }
            DropdownMenu(
                expanded = fabMenuOpen,
                onDismissRequest = { fabMenuOpen = false },
                containerColor = SurfaceWhite
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.portfolio_new_purchase), color = TextPrimary, fontSize = 14.sp) },
                    leadingIcon = { Text("↗", fontSize = 15.sp) },
                    onClick = { fabMenuOpen = false; onOpenAddTransactionSheet() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.portfolio_new_bond), color = TextPrimary, fontSize = 14.sp) },
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
