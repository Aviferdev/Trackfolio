package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

/**
 * Pantalla principal de portfolio. Muestra resumen agregado, distribución
 * por categorías, listado de posiciones abiertas agrupadas, y posiciones
 * cerradas en sección colapsable (decisión A).
 *
 * El FAB ofrece dos acciones: registrar un movimiento (lo más frecuente) o
 * dar de alta un activo nuevo en el catálogo.
 *
 * @param onAssetClick callback que dispara la navegación al historial del
 *        activo. Lo proporciona el NavHost.
 */
@Composable
fun PortfolioScreen(
    onAssetClick: (String) -> Unit = {},
    viewModel: PortfolioViewModel = koinViewModel(),
    catalogViewModel: AssetCatalogViewModel = koinViewModel(),
    platformViewModel: PlatformViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel()
) {
    val state               by viewModel.portfolioState.collectAsState()
    val catalogState        by catalogViewModel.uiState.collectAsState()
    val platformState       by platformViewModel.uiState.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val balancesHidden = LocalBalanceHidden.current

    accountViewModel.selectAccount()

    var fabMenuOpen     by remember { mutableStateOf(false) }
    var closedExpanded  by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { PortfolioHeader() }

            item {
                PortfolioSummaryCard(
                    totalInvested      = state.totalInvested,
                    totalCurrentValue  = state.totalCurrentValue,
                    totalPnL           = state.totalPnL,
                    totalPnLPercent    = state.totalPnLPercent,
                    totalRealizedPnL   = state.totalRealizedPnL,
                    totalUnrealizedPnL = state.totalUnrealizedPnL,
                    positionsCount     = state.openPositionsCount,
                    currencyCode       = state.currencyCode,
                    balancesHidden     = balancesHidden,
                    modifier           = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Donut chart de distribución por categoría ────────────────────
            if (state.distribution.isNotEmpty()) {
                item {
                    PortfolioDistributionCard(
                        slices            = state.distribution,
                        totalCurrentValue = state.totalCurrentValue,
                        currencyCode      = state.currencyCode,
                        balancesHidden    = balancesHidden,
                        modifier          = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }

            // ── Lista de grupos abiertos ─────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                }
            } else if (state.groups.isEmpty() && state.closedPositions.isEmpty()) {
                item { EmptyPortfolioState() }
            } else {
                state.groups.forEach { group ->
                    item(key = "header_${group.category?.id ?: "none"}") {
                        CategoryGroupHeader(
                            group          = group,
                            currencyCode   = state.currencyCode,
                            balancesHidden = balancesHidden
                        )
                    }
                    items(
                        items = group.rows,
                        key   = { row -> "open_${row.asset.id}" }
                    ) { row ->
                        AssetCard(
                            row            = row,
                            currencyCode   = state.currencyCode,
                            balancesHidden = balancesHidden,
                            onClick        = { onAssetClick(row.asset.id) },
                            onUpdatePrice  = { viewModel.openUpdatePriceSheet(row.asset) },
                            modifier       = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }

                // ── Sección colapsable de posiciones cerradas ───────────────
                if (state.closedPositions.isNotEmpty()) {
                    item(key = "closed_header") {
                        ClosedPositionsHeader(
                            count    = state.closedPositions.size,
                            expanded = closedExpanded,
                            onToggle = { closedExpanded = !closedExpanded }
                        )
                    }
                    item(key = "closed_list") {
                        AnimatedVisibility(
                            visible = closedExpanded,
                            enter   = expandVertically() + fadeIn(),
                            exit    = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                state.closedPositions.forEach { row ->
                                    ClosedAssetCard(
                                        row            = row,
                                        currencyCode   = state.currencyCode,
                                        balancesHidden = balancesHidden,
                                        onClick        = { onAssetClick(row.asset.id) },
                                        modifier       = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── FAB con menú ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 32.dp)
        ) {
            FloatingActionButton(
                onClick   = { fabMenuOpen = true },
                modifier  = Modifier.size(56.dp),
                shape          = CircleShape,
                containerColor = PrimaryDark,
                contentColor   = Color.White,
                elevation      = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light, color = Color.White)
            }
            DropdownMenu(
                expanded         = fabMenuOpen,
                onDismissRequest = { fabMenuOpen = false },
                containerColor   = SurfaceWhite
            ) {
                DropdownMenuItem(
                    text = { Text("Nuevo movimiento", color = TextPrimary) },
                    leadingIcon = { Text("💱", fontSize = 16.sp) },
                    onClick = {
                        fabMenuOpen = false
                        viewModel.openAddTransactionSheet()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Nuevo activo", color = TextPrimary) },
                    leadingIcon = { Text("📋", fontSize = 16.sp) },
                    onClick = {
                        fabMenuOpen = false
                        catalogViewModel.openAddSheet()
                    }
                )
            }
        }
    }

    // ── Sheets y diálogos ────────────────────────────────────────────────────

    // Nuevo movimiento (desde FAB)
    if (state.showAddTxSheet) {
        AddEditAssetTransactionBottomSheet(
            transaction       = null,
            fixedAsset        = null,
            allAssets         = state.allAssets,
            platforms         = state.platforms,
            assetTransactions = emptyList(), // sin asset preseleccionado, no se valida sobreventa hasta elegir
            currencyCode      = state.currencyCode,
            onSave            = { assetId, type, qty, price, date, platformId, feeNote, notes ->
                viewModel.addTransaction(assetId, type, qty, price, date, platformId, feeNote, notes)
            },
            onCreatePlatform  = {
                // Cierra la sheet de movimiento y abre la de creación de plataforma.
                // El usuario podrá crear la plataforma y luego volverá a abrir
                // el sheet de movimiento manualmente desde el FAB.
                viewModel.closeAddTransactionSheet()
                platformViewModel.openAddSheet()
            },
            onDismiss         = { viewModel.closeAddTransactionSheet() }
        )
    }

    // Sheet rápido de actualizar precio
    if (state.showUpdatePriceSheet && state.pricingAsset != null) {
        UpdateCurrentPriceSheet(
            asset        = state.pricingAsset!!,
            currencyCode = state.currencyCode,
            onConfirm    = { newPrice -> viewModel.refreshCurrentPrice(state.pricingAsset!!, newPrice) },
            onDismiss    = { viewModel.closeUpdatePriceSheet() }
        )
    }

    // Sheet de catálogo: nuevo activo / editar
    if (catalogState.showAddSheet) {
        AddEditAssetBottomSheet(
            asset        = null,
            categories   = availableCategories,
            currencyCode = state.currencyCode,
            onSave       = { ticker, name, notes, categoryId, currentPrice ->
                catalogViewModel.addAsset(ticker, name, notes, categoryId, currentPrice)
            },
            onDismiss = { catalogViewModel.closeAddSheet() }
        )
    }
    catalogState.editing?.let { editing ->
        AddEditAssetBottomSheet(
            asset        = editing,
            categories   = availableCategories,
            currencyCode = state.currencyCode,
            onSave       = { ticker, name, notes, categoryId, currentPrice ->
                catalogViewModel.editAsset(editing, ticker, name, notes, categoryId, currentPrice)
            },
            onDismiss    = { catalogViewModel.closeEditSheet() }
        )
    }

    // Sheet de plataforma (atajo inline cuando se intenta crear movimiento sin tenerlas)
    if (platformState.showAddSheet) {
        AddEditPlatformSheet(
            initial   = null,
            onSave    = { name, icon -> platformViewModel.addPlatform(name, icon) },
            onDismiss = { platformViewModel.closeAddSheet() }
        )
    }

    // Errores
    state.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor   = SurfaceWhite,
            title = { Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text  = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    catalogState.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { catalogViewModel.clearError() },
            containerColor   = SurfaceWhite,
            title = { Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text  = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { catalogViewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    platformState.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { platformViewModel.clearError() },
            containerColor   = SurfaceWhite,
            title = { Text("Error", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text  = { Text(msg, fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { platformViewModel.clearError() }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ─── Cabecera ─────────────────────────────────────────────────────────────────
@Composable
private fun PortfolioHeader() {
    Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text       = "Portfolio",
                fontSize   = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
        }
    }
}

// ─── Tarjeta resumen ──────────────────────────────────────────────────────────
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
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // ── Valor actual destacado ─────────────────────────────────────
            Text(
                text     = "Valor total",
                fontSize = 13.sp,
                color    = Color.White.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text          = maskAmount(formatAmount(totalCurrentValue), balancesHidden),
                    fontSize      = 34.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = symbol,
                    fontSize   = 18.sp,
                    color      = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            // ── Invertido + Beneficio destacados ───────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                MetricColumn(
                    label    = "Invertido",
                    primary  = "${maskAmount(formatAmount(totalInvested), balancesHidden)} $symbol",
                    color    = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(44.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
                MetricColumn(
                    label     = "Beneficio total",
                    primary   = if (totalPnL == 0.0)
                                    "—"
                                else "${if (totalPnL >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(totalPnL)), balancesHidden)} $symbol",
                    secondary = if (totalPnL == 0.0) null
                                else "${if (totalPnLPercent >= 0) "+" else "−"}${formatPercent1(abs(totalPnLPercent))}%",
                    color     = when {
                        totalPnL > 0 -> Color(0xFF66BB6A)
                        totalPnL < 0 -> Color(0xFFEF9A9A)
                        else         -> Color.White
                    },
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }

            // ── Desglose realizado / latente (si aplica) ─────────────────
            if (totalRealizedPnL != 0.0 && totalUnrealizedPnL != 0.0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PnLBreakdownChip(
                        label  = "Realizado",
                        amount = totalRealizedPnL,
                        symbol = symbol,
                        masked = balancesHidden
                    )
                    PnLBreakdownChip(
                        label  = "Latente",
                        amount = totalUnrealizedPnL,
                        symbol = symbol,
                        masked = balancesHidden
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Pie discreto: nº de posiciones abiertas ───────────────────
            Text(
                text     = "$positionsCount ${if (positionsCount == 1) "posición abierta" else "posiciones abiertas"}",
                fontSize = 11.sp,
                color    = Color.White.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    primary: String,
    color: Color,
    secondary: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
        Spacer(Modifier.height(4.dp))
        Text(
            text       = primary,
            fontSize   = 16.sp,
            color      = color,
            fontWeight = FontWeight.SemiBold,
            maxLines   = 1
        )
        if (secondary != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text       = secondary,
                fontSize   = 11.sp,
                color      = color.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PnLBreakdownChip(
    label: String,
    amount: Double,
    symbol: String,
    masked: Boolean
) {
    val color = when {
        amount > 0  -> Color(0xFF66BB6A)
        amount < 0  -> Color(0xFFEF9A9A)
        else        -> Color.White.copy(alpha = 0.6f)
    }
    Column {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
        Spacer(Modifier.height(2.dp))
        Text(
            text     = if (amount == 0.0) "—"
                       else "${if (amount >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(amount)), masked)} $symbol",
            fontSize = 12.sp,
            color    = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Cabecera de grupo de categoría ──────────────────────────────────────────
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
        else               -> TextSecondary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = 4.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(group.displayIcon, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = group.displayName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text     = "(${group.rows.size})",
                    fontSize = 12.sp,
                    color    = TextSecondary
                )
            }
            // % beneficio del grupo a la derecha
            if (group.totalPnL != 0.0) {
                Text(
                    text       = "${if (group.totalPnLPercent >= 0) "+" else "−"}${formatPercent1(abs(group.totalPnLPercent))}%",
                    fontSize   = 13.sp,
                    color      = pnlColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            ValueChip(
                label = "Invertido",
                value = "${maskAmount(formatAmount(group.totalInvested), balancesHidden)} $symbol"
            )
            ValueChip(
                label = "Actual",
                value = "${maskAmount(formatAmount(group.totalCurrentValue), balancesHidden)} $symbol"
            )
        }
    }
}

@Composable
private fun ValueChip(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ", fontSize = 12.sp, color = TextSecondary)
        Text(
            text       = value,
            fontSize   = 13.sp,
            color      = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Tarjeta de activo abierta ───────────────────────────────────────────────
@Composable
private fun AssetCard(
    row: AssetRow,
    currencyCode: String,
    balancesHidden: Boolean,
    onClick: () -> Unit,
    onUpdatePrice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val asset    = row.asset
    val pos      = row.position
    val symbol   = currencySymbol(currencyCode)
    val pnlColor = when {
        pos.totalPnL > 0 -> IncomeGreen
        pos.totalPnL < 0 -> ExpenseRed
        else             -> TextSecondary
    }

    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar ticker
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = asset.ticker.take(3),
                    fontSize   = if (asset.ticker.length > 3) 9.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    textAlign  = TextAlign.Center
                )
            }
            Spacer(Modifier.width(12.dp))

            // Nombre + qty × coste medio + frescura del precio
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = asset.name,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = TextPrimary,
                    maxLines   = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = "${formatQty(pos.netQuantity)} × ${maskAmount(formatAmount(pos.averageCostOfRemaining), balancesHidden)} $symbol",
                    fontSize = 12.sp,
                    color    = TextSecondary
                )
                if (pos.hasCurrentPrice && asset.currentPriceUpdatedAt != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = "actualizado ${formatRelativeTime(asset.currentPriceUpdatedAt!!)}",
                        fontSize = 10.sp,
                        color    = TextSecondary.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Valor + P&L
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = if (pos.hasCurrentPrice)
                        "${maskAmount(formatAmount(pos.currentValue), balancesHidden)} $symbol"
                    else "—",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary
                )
                if (pos.hasCurrentPrice) {
                    val prefix = if (pos.totalPnL >= 0) "+" else "−"
                    Text(
                        text     = "$prefix ${maskAmount(formatAmount(abs(pos.totalPnL)), balancesHidden)} $symbol",
                        fontSize = 12.sp,
                        color    = pnlColor
                    )
                    Text(
                        text     = "$prefix${formatPercent1(abs(pos.totalPnLPercent))}%",
                        fontSize = 11.sp,
                        color    = pnlColor.copy(alpha = 0.85f)
                    )
                } else {
                    Text(
                        text     = "Sin precio",
                        fontSize = 11.sp,
                        color    = TextSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // Botón rápido de actualizar precio
            IconButton(onClick = onUpdatePrice, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = "Actualizar precio",
                    modifier = Modifier.size(16.dp),
                    tint     = PrimaryDark
                )
            }
        }
    }
}

// ─── Sección de posiciones cerradas ──────────────────────────────────────────
@Composable
private fun ClosedPositionsHeader(
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✓", fontSize = 16.sp, color = TextSecondary)
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "Posiciones cerradas",
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextSecondary
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text     = "($count)",
                fontSize = 12.sp,
                color    = TextSecondary
            )
        }
        Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "Colapsar" else "Expandir",
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
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
    val pos   = row.position
    val symbol = currencySymbol(currencyCode)
    val pnlColor = when {
        pos.realizedPnL > 0 -> IncomeGreen
        pos.realizedPnL < 0 -> ExpenseRed
        else                -> TextSecondary
    }

    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TextSecondary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = asset.ticker.take(3),
                    fontSize   = if (asset.ticker.length > 3) 9.sp else 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextSecondary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = asset.name,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = TextPrimary,
                    maxLines   = 1
                )
                Text(
                    text       = "Cerrada · ${asset.ticker}",
                    fontSize   = 11.sp,
                    color      = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Realizado", fontSize = 10.sp, color = TextSecondary)
                Text(
                    text       = "${if (pos.realizedPnL >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(pos.realizedPnL)), balancesHidden)} $symbol",
                    fontSize   = 13.sp,
                    color      = pnlColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyPortfolioState() {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📈", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Sin posiciones",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Pulsa + para registrar\ntu primer movimiento",
                fontSize  = 14.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Helpers de formato locales ──────────────────────────────────────────────
private fun formatPercent1(value: Double): String {
    val rounded = (value * 10).toLong()
    return "${rounded / 10},${rounded % 10}"
}
