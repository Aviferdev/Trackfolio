package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.account.AccountViewModel
import es.aviferdev.n3to.ui.common.DeltaIndicator
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.common.LineChartWithTimeRange
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.component.IconActionButton
import es.aviferdev.n3to.ui.common.component.NavyTabRow
import es.aviferdev.n3to.ui.savingsrates.SavingsRatePreviewCard
import es.aviferdev.n3to.ui.fixedincome.EditFixedIncomeBottomSheet
import es.aviferdev.n3to.ui.common.button.IconButtonApp
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.fixedincome.CreateFixedIncomeBottomSheet
import es.aviferdev.n3to.ui.fixedincome.FixedIncomePositionCard
import es.aviferdev.n3to.ui.fixedincome.RegisterCouponBottomSheet

import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden

import es.aviferdev.n3to.ui.theme.PrimaryDark

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

@Composable
fun PortfolioScreen(
    onAssetClick: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onFixedIncomeClick: (String) -> Unit = {},
    onNavigateToSavingsRates: () -> Unit = {},
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

    // ── Configurar tabs ─────────────────────────────────────────────────────
    // tabTitles[0] = null (Todas), tabTitles[1+] = portfolio.id
    val tabTitles = remember(portfolios) {
        listOf(null) + portfolios.map { it.id }
    }

    var tabIndex by remember(portfolios) { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(
        pageCount = { tabTitles.size }
    )

    // Sincronizar pagerState → tabIndex → selectedPortfolioId
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                tabIndex = page
                val portfolioId = tabTitles.getOrNull(page)
                viewModel.selectPortfolio(portfolioId)
            }
    }

    val coroutineScope = rememberCoroutineScope()

    // ── Layout principal ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        Column(
            Modifier
                .fillMaxSize()
        ) {
            // Cabecera
            TopBarApp(
                title = stringResource(Res.string.portfolio_title),
                navigateBack = null,
                actions = {
                    IconActionButton(
                        onClick = onNavigateToSettings,
                        icon = Icons.Outlined.Settings,
                        iconTint = MaterialTheme.appColors.textSecondary,
                        label = stringResource(Res.string.portfolio_settings_cd)
                    )
                }
            )

            if (tabTitles.isNotEmpty()) {
                NavyTabRow(
                    items = tabTitles,
                    selected = tabTitles[tabIndex],
                    onSelect = { portfolioId ->
                        val page = tabTitles.indexOf(portfolioId)
                        coroutineScope.launch { pagerState.animateScrollToPage(page) }
                    },
                    label = { portfolioId ->
                        if (portfolioId == null) "Todas"
                        else portfolios.find { it.id == portfolioId }?.name ?: ""
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // HorizontalPager con contenido por tab
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val portId = tabTitles.getOrNull(page)
                PortfolioTabContent(
                    portfolioId = portId,
                    isTodasTab = portId == null,
                    state = state,
                    valueHistory = valueHistory,
                    balancesHidden = balancesHidden,
                    portfolios = portfolios,
                    onNavigateToSettings = onNavigateToSettings,
                    onAssetClick = onAssetClick,
                    onFixedIncomeClick = onFixedIncomeClick,
                    onNavigateToSavingsRates = onNavigateToSavingsRates,
                    onSelectDistributionView = { viewModel.selectDistributionView(it) },
                    onOpenUpdatePriceSheet = { asset -> viewModel.openUpdatePriceSheet(asset) },
                    onShowRegisterCouponSheet = { position -> viewModel.showRegisterCouponSheet(position) }
                )
            }
        }

        // FAB — solo visible cuando hay una cartera concreta seleccionada
        AnimatedVisibility(
            visible = selectedPortfolioId != null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 136.dp)
        ) {
            var fabMenuOpen by remember { mutableStateOf(false) }
            Column {
                FloatingActionButton(
                    onClick = { fabMenuOpen = true },
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.appColors.navySurface,
                    contentColor = MaterialTheme.appColors.cyanAccent,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp)
                ) {
                    Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = MaterialTheme.appColors.cyanAccent)
                }
                DropdownMenu(
                    expanded = fabMenuOpen,
                    onDismissRequest = { fabMenuOpen = false },
                    containerColor = MaterialTheme.appColors.surface
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.portfolio_new_purchase), color = MaterialTheme.appColors.textPrimary, fontSize = 14.sp) },
                        leadingIcon = { Text("↗", fontSize = 15.sp) },
                        onClick = { fabMenuOpen = false; viewModel.openAddTransactionSheet() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.portfolio_new_bond), color = MaterialTheme.appColors.textPrimary, fontSize = 14.sp) },
                        leadingIcon = { Icon("🏦".toMaterialIcon(), contentDescription = null, modifier = Modifier.size(18.dp)) },
                        onClick = { fabMenuOpen = false; viewModel.openCreateFixedIncomeSheet() }
                    )
                }
            }
        }
    }

    if (state.showAddTxSheet) {
        AddEditAssetTransactionBottomSheet(
            transaction = null, fixedAsset = null,
            allAssets = state.allAssets, platforms = state.platforms,
            platformsByAsset = state.platformsByAsset, categories = availableCategories,
            assetTransactions = emptyList(),  buyOnly = true,
            selectedPortfolioId = selectedPortfolioId,
            onSave = { assetId, type, qty, price, date, platformId, feeNote, notes, portfolioId ->
                viewModel.addTransaction(assetId, type, qty, price, date, platformId, feeNote, notes, portfolioId)
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
            selectedPortfolioId = selectedPortfolioId,
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
            onDismiss = { viewModel.closeUpdatePriceSheet() },
            detectAnomaly = viewModel.priceAnomalyDetector
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
            onValidateIsin = { identifier, categoryId -> catalogViewModel.validateIsin(identifier, categoryId) },
            onSave = { ticker, name, notes, categoryId, currentPrice, isin, platformIds, _, fixedPct, sectorIds, regionPercents, portfolioId ->
                catalogViewModel.addAsset(ticker, name, notes, categoryId, currentPrice, isin, platformIds, fixedPct, sectorIds, regionPercents, portfolioId)
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
            onValidateIsin = { identifier, categoryId -> catalogViewModel.validateIsin(identifier, categoryId) },
            onSave = { ticker, name, notes, categoryId, currentPrice, isin, platformIds, _, fixedPct, sectorIds, regionPercents, portfolioId ->
                catalogViewModel.editAsset(editing, ticker, name, notes, categoryId, currentPrice, isin, platformIds, fixedPct, sectorIds, regionPercents, portfolioId)
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
            containerColor = MaterialTheme.appColors.surface,
            title = { Text(stringResource(Res.string.common_error), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.appColors.textPrimary) },
            text = { Text(errorMsg, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary) },
            confirmButton = { TextButton(onClick = clearFn) { Text(stringResource(Res.string.common_accept), color = MaterialTheme.appColors.primary) } },
            shape = RoundedCornerShape(16.dp)
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
