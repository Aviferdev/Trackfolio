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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import es.aviferdev.trackfolio.domain.model.AssetCategoryType
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.portfolio.AssetPosition
import es.aviferdev.trackfolio.domain.portfolio.FifoBreakdown
import es.aviferdev.trackfolio.domain.portfolio.FifoOpenLot
import es.aviferdev.trackfolio.domain.portfolio.FifoSaleMatch
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

@Composable
fun AssetHistoryScreen(
    assetId: String,
    onBack: () -> Unit,
    viewModel: AssetHistoryViewModel = koinViewModel(parameters = { parametersOf(assetId) })
) {
    val state          by viewModel.uiState.collectAsState()
    val balancesHidden  = LocalBalanceHidden.current
    var fabMenuOpen    by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ───────────────────────────────────────────────────────
            AssetTopBar(
                ticker    = state.asset?.ticker,
                name      = state.asset?.name,
                onBack    = onBack,
                onRefresh = if (!AssetCategoryType.isFixedIncome(state.asset?.assetCategoryId ?: "")) {
                    { viewModel.openUpdatePriceSheet() }
                } else null
            )

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = PrimaryDark) }

                state.asset == null -> Box(
                    Modifier.fillMaxSize().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.error ?: "Activo no encontrado",
                        fontSize = 13.sp,
                        color    = TextTertiary
                    )
                }

                else -> LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Summary card (price + position)
                    item {
                        AssetSummaryCard(
                            ticker       = state.asset!!.ticker,
                            currentPrice = state.asset!!.currentPrice,
                            currencyCode = state.currencyCode,
                            position     = state.position,
                            modifier     = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    // Maturity (fixed income)
                    state.asset!!.maturityDate?.let { md ->
                        if (AssetCategoryType.isFixedIncome(state.asset!!.assetCategoryId)) {
                            item {
                                MaturityDateCard(
                                    maturityDate = md,
                                    modifier     = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // FIFO breakdown
                    state.breakdown?.takeIf { it.hasAnyData }?.let { bd ->
                        item {
                            FifoBreakdownSection(
                                breakdown      = bd,
                                currencyCode   = state.currencyCode,
                                balancesHidden = balancesHidden,
                                modifier       = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Transactions header
                    item {
                        SectionLabel(
                            "Movimientos",
                            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 6.dp)
                        )
                    }

                    if (state.transactionsDesc.isEmpty()) {
                        item { EmptyCard(modifier = Modifier.padding(horizontal = 16.dp)) }
                    } else {
                        items(state.transactionsDesc, key = { it.id }) { tx ->
                            TxRow(
                                tx             = tx,
                                platform       = state.platforms.firstOrNull { it.id == tx.platformId },
                                currencyCode   = state.currencyCode,
                                balancesHidden = balancesHidden,
                                onEdit         = { viewModel.openEditSheet(tx) },
                                onDelete       = { viewModel.requestDelete(tx) },
                                modifier       = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Dividends
                    if (state.dividends.isNotEmpty()) {
                        item {
                            SectionLabel(
                                "Dividendos",
                                modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 6.dp)
                            )
                        }
                        items(state.dividends, key = { it.id }) { dividend ->
                            DividendRow(
                                dividend       = dividend,
                                currencyCode   = state.currencyCode,
                                balancesHidden = balancesHidden,
                                onDelete       = { viewModel.deleteDividend(dividend.linkedAssetTransactionId ?: dividend.id) },
                                modifier       = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        if (state.asset != null) {
            val isFixedIncome = AssetCategoryType.isFixedIncome(state.asset!!.assetCategoryId)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 28.dp)
            ) {
                FloatingActionButton(
                    onClick        = { fabMenuOpen = true },
                    modifier       = Modifier.size(52.dp),
                    shape          = RoundedCornerShape(16.dp),
                    containerColor = PrimaryDark,
                    contentColor   = Color.White,
                    elevation      = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = Color.White)
                }
                DropdownMenu(
                    expanded         = fabMenuOpen,
                    onDismissRequest = { fabMenuOpen = false },
                    containerColor   = SurfaceWhite
                ) {
                    if (!isFixedIncome) {
                        DropdownMenuItem(
                            text        = { Text("Nuevo movimiento", color = TextPrimary, fontSize = 14.sp) },
                            leadingIcon = { Text("💱", fontSize = 14.sp) },
                            onClick     = { fabMenuOpen = false; viewModel.openAddSheet() }
                        )
                        DropdownMenuItem(
                            text        = { Text("Registrar dividendo", color = TextPrimary, fontSize = 14.sp) },
                            leadingIcon = { Text("📈", fontSize = 14.sp) },
                            onClick     = { fabMenuOpen = false; viewModel.openDividendSheet() }
                        )
                    }
                    if (state.isTransferable) {
                        DropdownMenuItem(
                            text        = { Text("Traspasar fondo", color = TextPrimary, fontSize = 14.sp) },
                            leadingIcon = { Text("🔄", fontSize = 14.sp) },
                            onClick     = { fabMenuOpen = false; viewModel.openTransferSheet() }
                        )
                    }
                }
            }
        }
    }

    // ── Sheets & dialogs (lógica intacta) ─────────────────────────────────────
    if (state.showAddSheet && state.asset != null) {
        AddEditAssetTransactionBottomSheet(
            transaction       = state.editing,
            fixedAsset        = state.asset,
            allAssets         = listOfNotNull(state.asset),
            platforms         = state.allPlatforms,
            platformsByAsset  = state.platformsByAsset,
            categories        = state.categories,
            assetTransactions = state.transactionsAsc,
            currencyCode      = state.currencyCode,
            onSave            = { _, type, qty, price, date, platformId, feeNote, notes ->
                viewModel.saveTransaction(type, qty, price, date, platformId, feeNote, notes)
            },
            onDismiss = { viewModel.closeAddSheet() }
        )
    }
    if (state.showUpdatePriceSheet && state.asset != null) {
        UpdateCurrentPriceSheet(
            asset        = state.asset!!,
            currencyCode = state.currencyCode,
            onConfirm    = { viewModel.refreshCurrentPrice(it) },
            onDismiss    = { viewModel.closeUpdatePriceSheet() }
        )
    }
    state.pendingDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor   = SurfaceWhite,
            icon  = { Text("⚠️", fontSize = 26.sp) },
            title = {
                Text(
                    if (tx.isTransfer) "Eliminar traspaso" else "Eliminar movimiento",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                )
            },
            text = {
                Text(
                    if (tx.isTransfer)
                        "Se eliminarán ambas patas del traspaso. El P&L se recalculará. Esta acción no se puede deshacer."
                    else
                        "Se eliminará el movimiento del ${formatFullDate(tx.date)}. El P&L se recalculará. Esta acción no se puede deshacer.",
                    fontSize = 13.sp, color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancelar", color = PrimaryDark) }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    state.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor   = SurfaceWhite,
            title = { Text("Error", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text  = { Text(msg, fontSize = 13.sp, color = TextSecondary) },
            confirmButton = { TextButton(onClick = { viewModel.clearError() }) { Text("Aceptar", color = PrimaryDark) } },
            shape = RoundedCornerShape(16.dp)
        )
    }
    if (state.showDividendSheet && state.asset != null) {
        AddDividendBottomSheet(
            fixedAssetName = state.asset!!.name,
            currencyCode   = state.currencyCode,
            onSave         = { _, grossAmount, irpfPercent, date -> viewModel.saveDividend(grossAmount, irpfPercent, date) },
            onDismiss      = { viewModel.closeDividendSheet() }
        )
    }
    if (state.showTransferSheet && state.asset != null) {
        TransferFundBottomSheet(
            sourceAsset       = state.asset!!,
            destinations      = state.transferableDestinations,
            platforms         = state.allPlatforms,
            assetTransactions = state.transactionsAsc,
            currencyCode      = state.currencyCode,
            onExecuteTransfer = { destId, qty, srcPlat, dstPlat, vl, date ->
                viewModel.executeTransfer(destId, qty, srcPlat, dstPlat, vl, date)
            },
            onDismiss = { viewModel.closeTransferSheet() }
        )
    }
}

// ─── Top bar ──────────────────────────────────────────────────────────────────
@Composable
private fun AssetTopBar(ticker: String?, name: String?, onBack: () -> Unit, onRefresh: (() -> Unit)? = null) {
    Surface(color = SurfaceWhite, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary)
            }
            Spacer(Modifier.width(2.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ticker ?: "—",
                    fontSize      = 17.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = TextPrimary,
                    letterSpacing = (-.3).sp
                )
                if (name != null) {
                    Text(name, fontSize = 12.sp, color = TextTertiary, maxLines = 1)
                }
            }
            if (onRefresh != null) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Refresh, "Actualizar precio", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
    HorizontalDivider(color = BorderGray, thickness = .5.dp)
}

// ─── Summary card (price + position) ───────────────────────────────────────────
@Composable
private fun AssetSummaryCard(
    ticker: String,
    currentPrice: Double?,
    currencyCode: String,
    position: AssetPosition?,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val isOpen = position?.netQuantity ?: 0.0 > 0.0

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 3-column grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // P&L FIFO
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("P&L FIFO", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    val pnlColor = when {
                        (position?.totalPnL ?: 0.0) > 0 -> IncomeGreen
                        (position?.totalPnL ?: 0.0) < 0 -> ExpenseRed
                        else -> TextPrimary
                    }
                    Text(
                        if ((position?.totalPnL ?: 0.0) == 0.0) "—"
                        else "${if ((position?.totalPnL ?: 0.0) >= 0) "+" else "−"} ${formatAmount(kotlin.math.abs(position?.totalPnL ?: 0.0))} €",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }

                // Divider
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderGray))

                // Posición
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Posición", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isOpen) "${formatQty(position!!.netQuantity)} uds" else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Divider
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderGray))

                // Coste medio
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Coste medio", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isOpen && position!!.averageCostOfRemaining > 0) "${formatAmount(position.averageCostOfRemaining)} €" else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }

            // Bottom row: Precio actual
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Precio actual", fontSize = 11.sp, color = TextTertiary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        currentPrice?.let { "${formatAmount(it)} €" } ?: "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (currentPrice != null && position?.averageCostOfRemaining != null && position.averageCostOfRemaining > 0) {
                        val pctChange = ((currentPrice - position.averageCostOfRemaining) / position.averageCostOfRemaining) * 100
                        Spacer(Modifier.width(8.dp))
                        val isPositive = pctChange >= 0
                        Text(
                            "${if (isPositive) "+" else "−"}${formatPercent1(kotlin.math.abs(pctChange))}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }
    }
}

// ─── Position card ────────────────────────────────────────────────────────────
@Composable
private fun PositionCard(
    position: AssetPosition,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val isOpen = position.netQuantity > 0.0
    val pnlColor = when {
        position.totalPnL > 0 -> Color(0xFF86EFAC)
        position.totalPnL < 0 -> Color(0xFFFCA5A5)
        else                  -> Color.White
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                PositionMetric("Cantidad",    if (isOpen) formatQty(position.netQuantity) else "—",    Color.White)
                PositionMetric("Coste medio", if (isOpen) "${maskAmount(formatAmount(position.averageCostOfRemaining), balancesHidden)} $symbol" else "—", Color.White, alignEnd = true)
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(.12f), thickness = .5.dp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                PositionMetric(
                    "Invertido",
                    if (isOpen) "${maskAmount(formatAmount(position.totalInvestedRemaining), balancesHidden)} $symbol" else "—",
                    Color.White.copy(.8f)
                )
                PositionMetric(
                    "Valor actual",
                    when {
                        !isOpen                -> "—"
                        position.hasCurrentPrice -> "${maskAmount(formatAmount(position.currentValue), balancesHidden)} $symbol"
                        else                   -> "Sin precio"
                    },
                    Color.White,
                    alignEnd = true
                )
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(.12f), thickness = .5.dp)
            Spacer(Modifier.height(12.dp))

            Text("Beneficio total", fontSize = 10.sp, color = Color.White.copy(.5f))
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (position.totalPnL == 0.0) "—"
                    else "${if (position.totalPnL >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(position.totalPnL)), balancesHidden)} $symbol",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = pnlColor
                )
                if (position.totalPnL != 0.0 && (isOpen || position.realizedPnL != 0.0)) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${if (position.totalPnLPercent >= 0) "+" else "−"}${formatPercent1(abs(position.totalPnLPercent))}%",
                        fontSize   = 12.sp,
                        color      = pnlColor.copy(.8f),
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            if (position.realizedPnL != 0.0 || position.unrealizedPnL != 0.0 || position.dividendIncome != 0.0) {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    PnLChip("Realizado",  position.realizedPnL,   symbol, balancesHidden)
                    if (position.dividendIncome != 0.0) PnLChip("Dividendos", position.dividendIncome, symbol, balancesHidden)
                    PnLChip("Latente",    position.unrealizedPnL, symbol, balancesHidden, unavailable = !position.hasCurrentPrice && isOpen)
                }
            }
        }
    }
}

@Composable
private fun PositionMetric(label: String, value: String, color: Color, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(.5f))
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 14.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PnLChip(label: String, amount: Double, symbol: String, masked: Boolean, unavailable: Boolean = false) {
    val text = when {
        unavailable   -> "Sin precio"
        amount == 0.0 -> "—"
        else          -> "${if (amount >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(amount)), masked)} $symbol"
    }
    val color = when {
        unavailable -> Color.White.copy(.45f)
        amount > 0  -> Color(0xFF86EFAC)
        amount < 0  -> Color(0xFFFCA5A5)
        else        -> Color.White.copy(.55f)
    }
    Column {
        Text(label, fontSize = 9.sp, color = Color.White.copy(.45f))
        Spacer(Modifier.height(2.dp))
        Text(text, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// ─── FIFO breakdown ───────────────────────────────────────────────────────────
@Composable
private fun FifoBreakdownSection(
    breakdown: FifoBreakdown,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val symbol   = currencySymbol(currencyCode)

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(13.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🧾", fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Desglose FIFO", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        val subtitle = buildString {
                            val o = breakdown.openLots.size; val s = breakdown.saleMatches.size
                            if (o > 0) append("$o ${if (o == 1) "lote en cartera" else "lotes en cartera"}")
                            if (o > 0 && s > 0) append("  ·  ")
                            if (s > 0) append("$s ${if (s == 1) "cierre" else "cierres"}")
                        }
                        Text(subtitle, fontSize = 10.sp, color = TextTertiary)
                    }
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    HorizontalDivider(color = BorderGray, thickness = .5.dp)
                    if (breakdown.openLots.isNotEmpty()) {
                        FifoSubHeader("Lotes en cartera")
                        breakdown.openLots.forEachIndexed { i, lot -> FifoOpenLotRow(i + 1, lot, symbol, balancesHidden) }
                    }
                    if (breakdown.saleMatches.isNotEmpty()) {
                        if (breakdown.openLots.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = BorderGray, thickness = .5.dp)
                        }
                        FifoSubHeader("Cierres FIFO")
                        breakdown.saleMatches.forEach { FifoSaleMatchBlock(it, symbol, balancesHidden) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FifoSubHeader(text: String) {
    Text(
        text.uppercase(),
        fontSize      = 9.sp,
        fontWeight    = FontWeight.Bold,
        color         = TextTertiary,
        letterSpacing = .5.sp,
        modifier      = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 5.dp)
    )
}

@Composable
private fun FifoOpenLotRow(index: Int, lot: FifoOpenLot, symbol: String, masked: Boolean) {
    val partial = lot.remainingQuantity < lot.originalQuantity
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(26.dp).clip(RoundedCornerShape(7.dp)).background(IncomeGreen.copy(.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text("#$index", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
        }
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${formatQty(lot.remainingQuantity)} u. × ${maskAmount(formatAmount(lot.pricePerUnit), masked)} $symbol",
                fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium
            )
            Text(
                "Comprado el ${formatShortDate(lot.purchaseDate)}${if (partial) "  ·  ${formatQty(lot.remainingQuantity)} de ${formatQty(lot.originalQuantity)} restantes" else ""}",
                fontSize = 10.sp, color = TextTertiary
            )
        }
        Text("${maskAmount(formatAmount(lot.remainingCost), masked)} $symbol", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FifoSaleMatchBlock(sale: FifoSaleMatch, symbol: String, masked: Boolean) {
    val pnlColor = when {
        sale.realizedPnL > 0 -> IncomeGreen
        sale.realizedPnL < 0 -> ExpenseRed
        else                 -> TextSecondary
    }
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("↘", fontSize = 13.sp, color = ExpenseRed, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(5.dp))
                    Text("Venta de ${formatQty(sale.saleQuantity)} u.", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Text("${formatShortDate(sale.saleDate)}  ·  ${maskAmount(formatAmount(sale.salePrice), masked)} $symbol/u.", fontSize = 10.sp, color = TextTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("P&L", fontSize = 9.sp, color = TextTertiary)
                Text(
                    if (sale.realizedPnL == 0.0) "—" else "${if (sale.realizedPnL >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(sale.realizedPnL)), masked)} $symbol",
                    fontSize = 12.sp, color = pnlColor, fontWeight = FontWeight.Bold
                )
            }
        }
        if (sale.consumed.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            sale.consumed.forEach { c ->
                val cColor = when { c.pnl > 0 -> IncomeGreen; c.pnl < 0 -> ExpenseRed; else -> TextSecondary }
                Row(Modifier.fillMaxWidth().padding(start = 14.dp, top = 2.dp, bottom = 2.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("↳ ${formatQty(c.quantityConsumed)} u. del lote del ${formatShortDate(c.purchaseDate)}", fontSize = 10.sp, color = TextPrimary)
                        Text("compra a ${maskAmount(formatAmount(c.purchasePrice), masked)} $symbol/u.", fontSize = 9.sp, color = TextTertiary)
                    }
                    Text(
                        if (c.pnl == 0.0) "—" else "${if (c.pnl >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(c.pnl)), masked)} $symbol",
                        fontSize = 10.sp, color = cColor, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Transaction row ──────────────────────────────────────────────────────────
@Composable
private fun TxRow(
    tx: AssetTransaction,
    platform: Platform?,
    currencyCode: String,
    balancesHidden: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val isBuy  = tx.type == AssetTransactionType.BUY || tx.type == AssetTransactionType.TRANSFER_IN
    val sideColor = if (isBuy) IncomeGreen else ExpenseRed
    val sideLabel = when (tx.type) {
        AssetTransactionType.BUY          -> "Compra"
        AssetTransactionType.SELL         -> "Venta"
        AssetTransactionType.TRANSFER_OUT -> "Traspaso salida"
        AssetTransactionType.TRANSFER_IN  -> "Traspaso entrada"
    }
    val sideIcon = when (tx.type) {
        AssetTransactionType.BUY          -> "↗"
        AssetTransactionType.SELL         -> "↘"
        AssetTransactionType.TRANSFER_OUT -> "→"
        AssetTransactionType.TRANSFER_IN  -> "←"
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(sideColor.copy(.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(sideIcon, fontSize = 16.sp, color = sideColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sideLabel, fontSize = 12.sp, color = sideColor, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Text("${formatQty(tx.quantity)} × ${maskAmount(formatAmount(tx.pricePerUnit), balancesHidden)} $symbol", fontSize = 11.sp, color = TextPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatShortDate(tx.date), fontSize = 10.sp, color = TextTertiary)
                    if (platform != null) {
                        Text("  ·  ", fontSize = 10.sp, color = TextTertiary)
                        Text(platform.icon, fontSize = 11.sp)
                        Spacer(Modifier.width(2.dp))
                        Text(platform.name, fontSize = 10.sp, color = TextTertiary)
                    }
                }
                if (!tx.feeNote.isNullOrBlank()) Text("Com: ${tx.feeNote}", fontSize = 9.sp, color = TextTertiary)
                if (!tx.notes.isNullOrBlank()) Text(tx.notes, fontSize = 9.sp, color = TextTertiary, maxLines = 2)
            }
            Spacer(Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isBuy) "+" else "−"} ${maskAmount(formatAmount(tx.grossAmount), balancesHidden)} $symbol",
                    fontSize = 12.sp, color = sideColor, fontWeight = FontWeight.Bold
                )
                Row {
                    if (!tx.isTransfer) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp), tint = TextSecondary)
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(13.dp), tint = ExpenseRed)
                    }
                }
            }
        }
    }
}

// ─── Dividend row ─────────────────────────────────────────────────────────────
@Composable
private fun DividendRow(
    dividend: Transaction,
    currencyCode: String,
    balancesHidden: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val gross  = dividend.grossAmount ?: dividend.amount
    val irpf   = if (dividend.grossAmount != null && dividend.irpfPercent != null)
        dividend.grossAmount * dividend.irpfPercent / 100.0 else 0.0

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(IncomeGreen.copy(.14f)),
                contentAlignment = Alignment.Center
            ) { Text("📈", fontSize = 16.sp) }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Dividendo", fontSize = 12.sp, color = IncomeGreen, fontWeight = FontWeight.SemiBold)
                Text(formatShortDate(dividend.date), fontSize = 10.sp, color = TextTertiary)
                if (irpf > 0) {
                    Text(
                        "Bruto: ${maskAmount(formatAmount(gross), balancesHidden)} $symbol  ·  IRPF: ${maskAmount(formatAmount(irpf), balancesHidden)} $symbol",
                        fontSize = 9.sp, color = TextTertiary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "+ ${maskAmount(formatAmount(dividend.amount), balancesHidden)} $symbol",
                    fontSize = 12.sp, color = IncomeGreen, fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(13.dp), tint = ExpenseRed)
                }
            }
        }
    }
}

// ─── Empty card ───────────────────────────────────────────────────────────────
@Composable
private fun EmptyCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(SurfaceWhite).padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📋", fontSize = 30.sp)
            Spacer(Modifier.height(8.dp))
            Text("Sin movimientos", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Pulsa + para registrar tu primera compra", fontSize = 11.sp, color = TextTertiary, textAlign = TextAlign.Center)
        }
    }
}

// ─── Maturity date card ───────────────────────────────────────────────────────
@Composable
private fun MaturityDateCard(maturityDate: Long, modifier: Modifier = Modifier) {
    val isExpired = maturityDate < Clock.System.now().toEpochMilliseconds()
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(13.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isExpired) ExpenseRed.copy(.08f) else SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (isExpired) "⏰" else "📅", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Vencimiento", fontSize = 10.sp, color = TextTertiary)
                Spacer(Modifier.height(2.dp))
                Text(
                    formatFullDate(maturityDate),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isExpired) ExpenseRed else TextPrimary
                )
                if (isExpired) Text("Vencido", fontSize = 11.sp, color = ExpenseRed, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─── Section label ────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        fontSize      = 10.sp,
        fontWeight    = FontWeight.Bold,
        color         = TextTertiary,
        letterSpacing = .7.sp,
        modifier      = modifier
    )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
private fun formatPercent1(value: Double): String {
    val r = (value * 10).toLong()
    return "${r / 10},${r % 10}"
}

private fun formatShortDate(epochMillis: Long): String {
    val months = listOf("ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic")
    val ld: LocalDate = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} ${months[ld.monthNumber - 1]} ${ld.year}"
}

private fun formatFullDate(epochMillis: Long): String {
    val months = listOf("enero","febrero","marzo","abril","mayo","junio","julio","agosto","septiembre","octubre","noviembre","diciembre")
    val ld: LocalDate = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}
