package es.aviferdev.trackfolio.ui.portfolio

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
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.common.DeltaIndicator
import es.aviferdev.trackfolio.ui.common.LineChartCard
import es.aviferdev.trackfolio.ui.common.button.IconButtomApp
import es.aviferdev.trackfolio.ui.fixedincome.CreateFixedIncomeBottomSheet
import es.aviferdev.trackfolio.ui.fixedincome.FixedIncomePositionCard
import es.aviferdev.trackfolio.ui.fixedincome.RegisterCouponBottomSheet
import es.aviferdev.trackfolio.ui.theme.BackgroundGray
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.BorderGray2
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.LocalBalanceHidden
import es.aviferdev.trackfolio.ui.theme.PrimaryAlpha
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceElevated
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.WarnAmber
import es.aviferdev.trackfolio.ui.theme.currencySymbol
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.maskAmount
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

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

    var closedExpanded by remember { mutableStateOf(false) }
    var fabMenuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            item { PortfolioTopBar(onSettingsClick = onNavigateToSettings) }

            // ── Summary card ──────────────────────────────────────────────────
            item {
                PortfolioSummaryCard(
                    totalInvested = state.totalInvested,
                    totalCurrentValue = state.totalCurrentValue,
                    totalPnL = state.totalPnL,
                    totalPnLPercent = state.totalPnLPercent,
                    totalRealizedPnL = state.totalRealizedPnL,
                    totalUnrealizedPnL = state.totalUnrealizedPnL,
                    positionsCount = state.openPositionsCount,
                    currencyCode = state.currencyCode,
                    balancesHidden = balancesHidden,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ── Line chart ────────────────────────────────────────────────────
            if (valueHistory.isNotEmpty()) {
                item {
                    LineChartCard(
                        title = "Evolución del valor",
                        subtitle = "Valor mensual del portfolio",
                        points = valueHistory.map { it.date to it.value },
                        lineColor = PrimaryDark,
                        currencyCode = state.currencyCode,
                        balancesHidden = balancesHidden,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Distribution tabs + donut ─────────────────────────────────────
            val hasDistribution = state.distribution.isNotEmpty()
                    || state.compositionDistribution.isNotEmpty()
                    || state.regionDistribution.isNotEmpty()
                    || state.sectorDistribution.isNotEmpty()

            if (hasDistribution) {
                item {
                    // Pill tabs (matching JSX design)
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
                                    .border(
                                        1.dp,
                                        if (selected) PrimaryDark else BorderGray2,
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable { viewModel.selectDistributionView(view) }
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
                        currencyCode = state.currencyCode,
                        balancesHidden = balancesHidden,
                        selectedView = state.selectedDistributionView,
                        fixedIncomePercent = state.fixedIncomeSummary?.let { fi ->
                            if (state.combinedCurrentValue > 0) (fi.totalCurrentValue / state.combinedCurrentValue) * 100 else 0.0
                        } ?: 0.0,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Asset groups ──────────────────────────────────────────────────
            when {
                state.isLoading -> item {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = PrimaryDark) }
                }

                state.groups.isEmpty() && state.closedPositions.isEmpty() -> item {
                    EmptyPortfolioState()
                }

                else -> {
                    // El listado siempre muestra los grupos por categoría
                    // (solo el gráfico de distribución cambia según la vista seleccionada)
                    state.groups.forEach { group ->
                        item(key = "hdr_${group.category?.id ?: "none"}") {
                            CategoryGroupHeader(
                                group = group,
                                currencyCode = state.currencyCode,
                                balancesHidden = balancesHidden
                            )
                        }
                        items(group.rows, key = { "open_${it.asset.id}" }) { row ->
                            AssetCard(
                                row = row,
                                currencyCode = state.currencyCode,
                                balancesHidden = balancesHidden,
                                onClick = { onAssetClick(row.asset.id) },
                                onUpdatePrice = { viewModel.openUpdatePriceSheet(row.asset) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                            )
                        }
                        if (group.fixedIncomeRows.isNotEmpty()) {
                            // Header de sección: "🏦 Renta fija" (como en JSX)
                            item(key = "fi_hdr_${group.category?.id ?: "none"}") {
                                FixedIncomeSectionHeader(
                                    count = group.fixedIncomeRows.size,
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 8.dp,
                                        bottom = 4.dp
                                    )
                                )
                            }
                            items(
                                group.fixedIncomeRows,
                                key = { "fi_${it.position.id}" }) { fiRow ->
                                FixedIncomePositionCard(
                                    row = fiRow,
                                    currencyCode = state.currencyCode,
                                    balancesHidden = balancesHidden,
                                    onClick = { onFixedIncomeClick(fiRow.position.id) },
                                    onRegisterCoupon = if (fiRow.position.hasPeriodicCoupons) {
                                        { viewModel.showRegisterCouponSheet(fiRow.position) }
                                    } else null,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    val totalClosedCount =
                        state.closedPositions.size + state.closedFixedIncomePositions.size
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
                                    // Activos cerrados
                                    state.closedPositions.forEach { row ->
                                        ClosedAssetCard(
                                            row = row,
                                            currencyCode = state.currencyCode,
                                            balancesHidden = balancesHidden,
                                            onClick = { onAssetClick(row.asset.id) },
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 5.dp
                                            )
                                        )
                                    }
                                    // Posiciones de renta fija cerradas
                                    state.closedFixedIncomePositions.forEach { fiRow ->
                                        ClosedFixedIncomeCard(
                                            row = fiRow,
                                            currencyCode = state.currencyCode,
                                            balancesHidden = balancesHidden,
                                            onClick = { onFixedIncomeClick(fiRow.position.id) },
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 5.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp)
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
                    onClick = { fabMenuOpen = false; viewModel.openAddTransactionSheet() }
                )
                DropdownMenuItem(
                    text = { Text("Nuevo bono/depósito", color = TextPrimary, fontSize = 14.sp) },
                    leadingIcon = { Text("🏦", fontSize = 14.sp) },
                    onClick = { fabMenuOpen = false; viewModel.openCreateFixedIncomeSheet() }
                )
            }
        }
    }

    // ── Sheets & dialogs (lógica intacta) ─────────────────────────────────────
    if (state.showAddTxSheet) {
        AddEditAssetTransactionBottomSheet(
            transaction = null, fixedAsset = null,
            allAssets = state.allAssets, platforms = state.platforms,
            platformsByAsset = state.platformsByAsset, categories = availableCategories,
            assetTransactions = emptyList(), currencyCode = state.currencyCode, buyOnly = true,
            onSave = { assetId, type, qty, price, date, platformId, feeNote, notes ->
                viewModel.addTransaction(
                    assetId,
                    type,
                    qty,
                    price,
                    date,
                    platformId,
                    feeNote,
                    notes
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
            asset = state.pricingAsset!!, currencyCode = state.currencyCode,
            onConfirm = { newPrice ->
                viewModel.refreshCurrentPrice(
                    state.pricingAsset!!,
                    newPrice
                )
            },
            onDismiss = { viewModel.closeUpdatePriceSheet() }
        )
    }
    if (catalogState.showAddSheet) {
        AddEditAssetBottomSheet(
            asset = null,
            categories = availableCategories,
            currencyCode = state.currencyCode,
            allPlatforms = state.platforms,
            allSectors = state.allSectors,
            linkedSectorIds = emptySet(),
            allRegions = state.allRegions,
            linkedRegionPercents = emptyMap(),
            onSave = { ticker, name, notes, categoryId, currentPrice, platformIds, _, fixedPct, sectorIds, regionPercents ->
                catalogViewModel.addAsset(
                    ticker,
                    name,
                    notes,
                    categoryId,
                    currentPrice,
                    platformIds,
                    fixedPct,
                    sectorIds,
                    regionPercents
                )
            },
            onDismiss = { catalogViewModel.closeAddSheet() }
        )
    }
    catalogState.editing?.let { editing ->
        AddEditAssetBottomSheet(
            asset = editing,
            categories = availableCategories,
            currencyCode = state.currencyCode,
            allPlatforms = state.platforms,
            linkedPlatformIds = catalogState.editingPlatformIds,
            allSectors = state.allSectors,
            linkedSectorIds = catalogState.editingSectorIds,
            allRegions = state.allRegions,
            linkedRegionPercents = catalogState.editingRegionPercents,
            linkedFixedIncomePercent = catalogState.editingFixedIncomePercent,
            onSave = { ticker, name, notes, categoryId, currentPrice, platformIds, _, fixedPct, sectorIds, regionPercents ->
                catalogViewModel.editAsset(
                    editing,
                    ticker,
                    name,
                    notes,
                    categoryId,
                    currentPrice,
                    platformIds,
                    fixedPct,
                    sectorIds,
                    regionPercents
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
    for (msg in listOfNotNull(state.error, catalogState.error, platformState.error)) {
        val clearFn: () -> Unit = when (msg) {
            state.error -> {
                { viewModel.clearError() }
            }

            catalogState.error -> {
                { catalogViewModel.clearError() }
            }

            else -> {
                { platformViewModel.clearError() }
            }
        }
        AlertDialog(
            onDismissRequest = clearFn,
            containerColor = SurfaceWhite,
            title = {
                Text(
                    "Error",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = { Text(msg, fontSize = 13.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = clearFn) {
                    Text(
                        "Aceptar",
                        color = PrimaryDark
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
        break // show one at a time
    }
}

// ─── Top bar ─────────────────────────────────────────────────────────────────
@Composable
private fun PortfolioTopBar(onSettingsClick: () -> Unit) {
    Surface(color = SurfaceWhite, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Portfolio",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.3).sp
            )
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(SurfaceElevated)
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Ajustes de portfolio",
                    tint = TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
    HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
}

// ─── Summary card ─────────────────────────────────────────────────────────────
@Composable
private fun PortfolioSummaryCard(
    totalInvested: Double,
    totalCurrentValue: Double,
    totalPnL: Double,
    totalPnLPercent: Double,
    totalRealizedPnL: Double,
    totalUnrealizedPnL: Double,
    positionsCount: Int,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text("Valor total", fontSize = 12.sp, color = Color.White.copy(alpha = 0.55f))
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    maskAmount(formatAmount(totalCurrentValue), balancesHidden),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    symbol,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                PortfolioMetric(
                    label = "Invertido",
                    primary = "${maskAmount(formatAmount(totalInvested), balancesHidden)} $symbol",
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .width(0.5.dp)
                        .height(44.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                )
                PortfolioMetric(
                    label = "Beneficio total",
                    primary = if (totalPnL == 0.0) "—"
                    else "${if (totalPnL >= 0) "+" else "−"} ${
                        maskAmount(
                            formatAmount(abs(totalPnL)),
                            balancesHidden
                        )
                    } $symbol",
                    secondary = if (totalPnL == 0.0) null
                    else "${if (totalPnLPercent >= 0) "+" else "−"}${
                        formatPercent1(
                            abs(
                                totalPnLPercent
                            )
                        )
                    }%",
                    color = when {
                        totalPnL > 0 -> Color(0xFF86EFAC)
                        totalPnL < 0 -> Color(0xFFFCA5A5)
                        else -> Color.White
                    },
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }
            if (totalRealizedPnL != 0.0 && totalUnrealizedPnL != 0.0) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    PnLChip("Realizado", totalRealizedPnL, symbol, balancesHidden)
                    PnLChip("Latente", totalUnrealizedPnL, symbol, balancesHidden)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "$positionsCount ${if (positionsCount == 1) "posición abierta" else "posiciones abiertas"}",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun PortfolioMetric(
    label: String,
    primary: String,
    color: Color,
    secondary: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
        Spacer(Modifier.height(4.dp))
        Text(primary, fontSize = 15.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1)
        if (secondary != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                secondary,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PnLChip(label: String, amount: Double, symbol: String, masked: Boolean) {
    val color = when {
        amount > 0 -> Color(0xFF86EFAC)
        amount < 0 -> Color(0xFFFCA5A5)
        else -> Color.White.copy(alpha = 0.5f)
    }
    Column {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.45f))
        Spacer(Modifier.height(2.dp))
        Text(
            if (amount == 0.0) "—"
            else "${if (amount >= 0) "+" else "−"} ${
                maskAmount(
                    formatAmount(abs(amount)),
                    masked
                )
            } $symbol",
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Category group header ────────────────────────────────────────────────────
@Composable
private fun CategoryGroupHeader(
    group: CategoryGroup,
    currencyCode: String,
    balancesHidden: Boolean
) {
    val symbol = currencySymbol(currencyCode)
    val pnlColor = when {
        group.totalPnL > 0 -> IncomeGreen
        group.totalPnL < 0 -> ExpenseRed
        else -> TextSecondary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(group.displayIcon, fontSize = 17.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    group.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.width(6.dp))
                Text("(${group.rowCount})", fontSize = 11.sp, color = TextTertiary)
            }
            if (group.totalPnL != 0.0) {
                DeltaIndicator(
                    value = "${if (group.totalPnLPercent >= 0) "+" else "−"}${
                        formatPercent1(
                            abs(
                                group.totalPnLPercent
                            )
                        )
                    }%",
                    isPositive = group.totalPnLPercent >= 0
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(
                "Invertido: ${
                    maskAmount(
                        formatAmount(group.totalInvested),
                        balancesHidden
                    )
                } $symbol",
                fontSize = 11.sp,
                color = TextTertiary
            )
            Text(
                "Actual: ${
                    maskAmount(
                        formatAmount(group.totalCurrentValue),
                        balancesHidden
                    )
                } $symbol",
                fontSize = 11.sp,
                color = TextTertiary
            )
        }
    }
}

// ─── Asset card ───────────────────────────────────────────────────────────────
@Composable
private fun AssetCard(
    row: AssetRow,
    currencyCode: String,
    balancesHidden: Boolean,
    onClick: () -> Unit,
    onUpdatePrice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val asset = row.asset
    val pos = row.position
    val symbol = currencySymbol(currencyCode)
    val pnlColor = when {
        pos.totalPnL > 0 -> IncomeGreen
        pos.totalPnL < 0 -> ExpenseRed
        else -> TextSecondary
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ticker badge with colored border (matching JSX)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(PrimaryAlpha)
                    .border(1.dp, PrimaryDark.copy(alpha = 0.25f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    asset.ticker.take(4),
                    fontSize = if (asset.ticker.length > 4) 8.sp else 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.3).sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    asset.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${formatQty(pos.netQuantity)} × ${
                        maskAmount(
                            formatAmount(pos.averageCostOfRemaining),
                            balancesHidden
                        )
                    } $symbol",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (pos.hasCurrentPrice)
                        "${maskAmount(formatAmount(pos.currentValue), balancesHidden)} $symbol"
                    else "—",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (pos.hasCurrentPrice) {
                    Text(
                        "${if (pos.totalPnL >= 0) "+" else "−"} ${
                            maskAmount(
                                formatAmount(abs(pos.totalPnL)),
                                balancesHidden
                            )
                        } $symbol",
                        fontSize = 11.sp,
                        color = pnlColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(
                modifier = Modifier.width(4.dp)
            )
            IconButtomApp(
                clickButton = onUpdatePrice,
                icon = Icons.Outlined.Refresh,
                contentDescription = "Actualizar precio"
            )
        }
    }
}

// ─── Closed positions ─────────────────────────────────────────────────────────
@Composable
private fun ClosedPositionsHeader(count: Int, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✓", fontSize = 14.sp, color = TextTertiary)
            Spacer(Modifier.width(8.dp))
            Text(
                "Posiciones cerradas",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextTertiary
            )
            Spacer(Modifier.width(5.dp))
            Text("($count)", fontSize = 11.sp, color = TextTertiary)
        }
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "Colapsar" else "Expandir",
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ClosedAssetCard(
    row: AssetRow,
    currencyCode: String,
    balancesHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val asset = row.asset
    val pos = row.position
    val symbol = currencySymbol(currencyCode)
    val pnlColor = when {
        pos.realizedPnL > 0 -> IncomeGreen
        pos.realizedPnL < 0 -> ExpenseRed
        else -> TextSecondary
    }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ticker badge for closed position
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    asset.ticker.take(3),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    asset.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    maxLines = 1
                )
                Text(
                    "Cerrada",
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Realizado",
                    fontSize = 10.sp,
                    color = TextTertiary
                )
                Text(
                    if (pos.realizedPnL == 0.0) "—"
                    else "${if (pos.realizedPnL >= 0) "+" else "−"} ${
                        maskAmount(
                            formatAmount(
                                abs(
                                    pos.realizedPnL
                                )
                            ), balancesHidden
                        )
                    } $symbol",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
            }
        }
    }
}

// ─── Fixed Income Section Header (JSX: 🏦 Renta fija) ──────────────────────
@Composable
private fun FixedIncomeSectionHeader(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🏦", fontSize = 15.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Renta fija",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = "($count)",
            fontSize = 11.sp,
            color = TextTertiary
        )
    }
}

// ─── Closed Fixed Income Card (JSX: posiciones cerradas de renta fija) ───────
@Composable
private fun ClosedFixedIncomeCard(
    row: es.aviferdev.trackfolio.domain.model.FixedIncomeRow,
    currencyCode: String,
    balancesHidden: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val position = row.position
    val symbol = currencySymbol(currencyCode)
    val pnlColor = when {
        row.totalProfit > 0 -> IncomeGreen
        row.totalProfit < 0 -> ExpenseRed
        else -> TextSecondary
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(WarnAmber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalance,
                    contentDescription = null,
                    tint = WarnAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    position.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    maxLines = 1
                )
                Text(
                    "Cerrada",
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Realizado",
                    fontSize = 10.sp,
                    color = TextTertiary
                )
                Text(
                    if (row.totalProfit == 0.0) "—"
                    else "${if (row.totalProfit >= 0) "+" else "−"} ${
                        maskAmount(
                            formatAmount(
                                kotlin.math.abs(
                                    row.totalProfit
                                )
                            ), balancesHidden
                        )
                    } $symbol",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
            }
        }
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyPortfolioState() {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📈", fontSize = 44.sp)
            Spacer(Modifier.height(14.dp))
            Text(
                "Sin posiciones",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Pulsa + para registrar\ntu primera inversión",
                fontSize = 13.sp,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatPercent1(value: Double): String {
    val rounded = (value * 10).toLong()
    return "${rounded / 10},${rounded % 10}"
}
