package es.aviferdev.n3to.ui.portfolio.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.ui.account.AccountViewModel
import es.aviferdev.n3to.ui.common.button.FloatingButtonAdd
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.component.NavyTabRow
import es.aviferdev.n3to.ui.common.loading.GlobalLoadingManager
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.common.topbar.TopBarWithoutActionsApp
import es.aviferdev.n3to.ui.fixedincome.CreateFixedIncomeBottomSheet
import es.aviferdev.n3to.ui.fixedincome.RegisterCouponBottomSheet
import es.aviferdev.n3to.ui.portfolio.AddEditAssetBottomSheet
import es.aviferdev.n3to.ui.portfolio.AddEditAssetTransactionBottomSheet
import es.aviferdev.n3to.ui.portfolio.AddEditPlatformSheet
import es.aviferdev.n3to.ui.portfolio.AssetCatalogViewModel
import es.aviferdev.n3to.ui.portfolio.CatalogError
import es.aviferdev.n3to.ui.portfolio.PlatformError
import es.aviferdev.n3to.ui.portfolio.PlatformViewModel
import es.aviferdev.n3to.ui.portfolio.PortfolioTabContent
import es.aviferdev.n3to.ui.portfolio.UpdateCurrentPriceSheet
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.coroutines.launch
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.error_account_required
import n3to.composeapp.generated.resources.error_asset_already_exists
import n3to.composeapp.generated.resources.error_asset_not_found
import n3to.composeapp.generated.resources.error_asset_ticker_required
import n3to.composeapp.generated.resources.error_cannot_archive_with_open_positions_qty
import n3to.composeapp.generated.resources.error_coupon_register
import n3to.composeapp.generated.resources.error_fi_create_position
import n3to.composeapp.generated.resources.error_fi_no_buy_sell
import n3to.composeapp.generated.resources.error_no_account_selected
import n3to.composeapp.generated.resources.error_platform_already_exists
import n3to.composeapp.generated.resources.networth_no_account_subtitle
import n3to.composeapp.generated.resources.networth_no_account_title
import n3to.composeapp.generated.resources.portfolio_new_bond
import n3to.composeapp.generated.resources.portfolio_new_purchase
import n3to.composeapp.generated.resources.portfolio_no_portfolios_subtitle
import n3to.composeapp.generated.resources.portfolio_no_portfolios_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PortfolioScreen(
    onAssetClick: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onFixedIncomeClick: (String) -> Unit = {},
    onNavigateToSavingsRates: () -> Unit = {},
    viewModel: PortfolioViewModel = koinViewModel(),
    catalogViewModel: AssetCatalogViewModel = koinViewModel(),
    platformViewModel: PlatformViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel(),
) {
    val loadingManager = koinInject<GlobalLoadingManager>()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        (uiState as? PortfolioNewUiState.Loading)?.let {
            loadingManager.show(it.message)
        } ?: run {
            loadingManager.hide()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        when (val state = uiState) {
            is PortfolioNewUiState.Loading -> Unit

            is PortfolioNewUiState.Error -> @Composable {
                Box(
                    Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.message,
                        color = MaterialTheme.appColors.expense
                    )
                }
            }

            is PortfolioNewUiState.Empty -> Emptya(
                onNavigateToSettings
            )

            is PortfolioNewUiState.EmptyPortFolio -> Empty(
                onNavigateToSettings = onNavigateToSettings
            )

            is PortfolioNewUiState.Success -> Fill(
                state.data.portfolios,
                onClickPortfolio = {},
                onNavigateToSettings = {},
                onAssetClick = {},
                onFixedIncomeClick = {},
                onNavigateToSavingsRates = {},
            )
        }
    }
}

@Composable
private fun Emptya(
    onNavigateToSettings: () -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep),
        contentAlignment = Alignment.Center
    ) {
        EmptyStateView(
            icon = "\uD83C\uDFE6",
            title = stringResource(Res.string.networth_no_account_title),
            subtitle = stringResource(Res.string.networth_no_account_subtitle),
            actionLabel = "Ir a Ajustes",
            onAction = onNavigateToSettings
        )
    }
}

@Composable
private fun Empty(
    onNavigateToSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 100.dp)
    ) {
        TopBarWithoutActionsApp(
            onNavigateToSettings = onNavigateToSettings
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateView(
                icon = "📂",
                title = stringResource(Res.string.portfolio_no_portfolios_title),
                subtitle = stringResource(Res.string.portfolio_no_portfolios_subtitle),
                actionLabel = "Ir a ajustes",
                onAction = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun Fill(
    listPortfolio: List<Portfolio>,
    onClickPortfolio: (portfolioId: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onAssetClick: (String) -> Unit = {},
    onFixedIncomeClick: (String) -> Unit = {},
    onNavigateToSavingsRates: () -> Unit = {},
    viewModel: PortfolioViewModel = koinViewModel(),
    catalogViewModel: AssetCatalogViewModel = koinViewModel(),
    platformViewModel: PlatformViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel(),
) {

    val state by viewModel.portfolioState.collectAsState()
    val catalogState by catalogViewModel.uiState.collectAsState()
    val platformState by platformViewModel.uiState.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val valueHistory by viewModel.portfolioValueHistory.collectAsState()
    val selectedPortfolioId by viewModel.selectedPortfolioId.collectAsState()

    val tabTitles = remember(listPortfolio) {
        listOf(null) + listPortfolio.map { it.id }
    }

    var tabIndex by remember(listPortfolio) { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(
        pageCount = { tabTitles.size }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                tabIndex = page
                tabTitles.getOrNull(page)?.let {
                    onClickPortfolio(it)
                }
            }
    }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        Column(
            Modifier
                .fillMaxSize()
        ) {
            TopBarWithoutActionsApp(
                onNavigateToSettings = onNavigateToSettings
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
                        else listPortfolio.find { it.id == portfolioId }?.name ?: ""
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
                    portfolios = listPortfolio,
                    onNavigateToSettings = onNavigateToSettings,
                    onAssetClick = onAssetClick,
                    onFixedIncomeClick = onFixedIncomeClick,
                    onNavigateToSavingsRates = onNavigateToSavingsRates,
                    onSelectDistributionView = { viewModel.selectDistributionView(it) },
                    onOpenUpdatePriceSheet = { asset -> viewModel.openUpdatePriceSheet(asset) },
                    onShowRegisterCouponSheet = { position ->
                        viewModel.showRegisterCouponSheet(
                            position
                        )
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = selectedPortfolioId != null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 136.dp)
        ) {
            var fabMenuOpen by remember { mutableStateOf(false) }
            Column {
                FloatingButtonAdd(
                    onClick = { fabMenuOpen = true },
                    modifier = Modifier.size(52.dp),
                )
                DropdownMenu(
                    expanded = fabMenuOpen,
                    onDismissRequest = { fabMenuOpen = false },
                    containerColor = MaterialTheme.appColors.surface
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(Res.string.portfolio_new_purchase),
                                color = MaterialTheme.appColors.textPrimary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = { Text("↗", fontSize = 15.sp) },
                        onClick = { fabMenuOpen = false; viewModel.openAddTransactionSheet() }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(Res.string.portfolio_new_bond),
                                color = MaterialTheme.appColors.textPrimary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                "🏦".toMaterialIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            fabMenuOpen = false; viewModel.openCreateFixedIncomeSheet()
                        }
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
            assetTransactions = emptyList(), buyOnly = true,
            selectedPortfolioId = selectedPortfolioId,
            onSave = { assetId, type, qty, price, date, platformId, feeNote, notes, portfolioId ->
                viewModel.addTransaction(
                    assetId,
                    type,
                    qty,
                    price,
                    date,
                    platformId,
                    feeNote,
                    notes,
                    portfolioId
                )
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
            onConfirm = { newPrice ->
                viewModel.refreshCurrentPrice(
                    state.pricingAsset!!,
                    newPrice
                )
            },
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
            portfolios = listPortfolio,
            onValidateIsin = { identifier, categoryId ->
                catalogViewModel.validateIsin(
                    identifier,
                    categoryId
                )
            },
            onSave = { ticker, name, notes, categoryId, currentPrice, isin, platformIds, _, fixedPct, sectorIds, regionPercents, portfolioId ->
                catalogViewModel.addAsset(
                    ticker,
                    name,
                    notes,
                    categoryId,
                    currentPrice,
                    isin,
                    platformIds,
                    fixedPct,
                    sectorIds,
                    regionPercents,
                    portfolioId
                )
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
            portfolios = listPortfolio,
            selectedPortfolioId = editing.portfolioId,
            onValidateIsin = { identifier, categoryId ->
                catalogViewModel.validateIsin(
                    identifier,
                    categoryId
                )
            },
            onSave = { ticker, name, notes, categoryId, currentPrice, isin, platformIds, _, fixedPct, sectorIds, regionPercents, portfolioId ->
                catalogViewModel.editAsset(
                    editing,
                    ticker,
                    name,
                    notes,
                    categoryId,
                    currentPrice,
                    isin,
                    platformIds,
                    fixedPct,
                    sectorIds,
                    regionPercents,
                    portfolioId
                )
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
    val stateError = state.error
    val catalogError = catalogState.error
    val platformError = platformState.error
    if (stateError != null || catalogError != null || platformError != null) {
        val errorMsg: String = when {
            stateError != null -> when (stateError) {
                is PortfolioSheetError.FiNoBuySell -> stringResource(Res.string.error_fi_no_buy_sell)
                is PortfolioSheetError.PriceHistorySave -> stateError.message
                is PortfolioSheetError.AssetNotFound -> stringResource(Res.string.error_asset_not_found)
                is PortfolioSheetError.FiCreatePosition -> stringResource(Res.string.error_fi_create_position)
                is PortfolioSheetError.CouponRegister -> stringResource(Res.string.error_coupon_register)
                is PortfolioSheetError.NoAccountSelected -> stringResource(Res.string.error_no_account_selected)
                is PortfolioSheetError.Unknown -> stateError.message
                    ?: stringResource(Res.string.common_error)
            }

            catalogError != null -> when (catalogError) {
                is CatalogError.AccountRequired -> stringResource(Res.string.error_account_required)
                is CatalogError.TickerAndNameRequired -> stringResource(Res.string.error_asset_ticker_required)
                is CatalogError.AssetAlreadyExists -> stringResource(
                    Res.string.error_asset_already_exists,
                    catalogError.ticker
                )

                is CatalogError.CannotArchiveWithOpenPositions -> stringResource(
                    Res.string.error_cannot_archive_with_open_positions_qty,
                    catalogError.ticker,
                    catalogError.qty
                )

                is CatalogError.Unknown -> catalogError.message
                    ?: stringResource(Res.string.common_error)
            }

            platformError != null -> when (platformError) {
                is PlatformError.AlreadyExists -> stringResource(Res.string.error_platform_already_exists)
                is PlatformError.Unknown -> platformError.message
                    ?: stringResource(Res.string.common_error)
            }

            else -> stringResource(Res.string.common_error)
        }
        val clearFn: () -> Unit = when {
            stateError != null -> {
                { viewModel.clearError() }
            }

            catalogError != null -> {
                { catalogViewModel.clearError() }
            }

            else -> {
                { platformViewModel.clearError() }
            }
        }
        AlertDialog(
            onDismissRequest = clearFn,
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.common_error),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    errorMsg,
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = clearFn) {
                    Text(
                        stringResource(Res.string.common_accept),
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}