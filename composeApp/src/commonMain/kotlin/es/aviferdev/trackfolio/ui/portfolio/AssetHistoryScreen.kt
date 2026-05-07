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
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.FixedIncomeCategories
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

/**
 * Pantalla dedicada al historial de un activo concreto (decisión 6.A).
 *
 * Muestra la cabecera del activo con su precio actual, la tarjeta de posición
 * FIFO calculada por [es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator]
 * y la lista cronológica de movimientos. El usuario puede añadir, editar y
 * borrar movimientos desde aquí.
 */
@Composable
fun AssetHistoryScreen(
    assetId: String,
    onBack: () -> Unit,
    viewModel: AssetHistoryViewModel = koinViewModel(parameters = { parametersOf(assetId) })
) {
    val state by viewModel.uiState.collectAsState()
    val balancesHidden = LocalBalanceHidden.current
    var fabMenuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AssetHistoryHeader(
                ticker = state.asset?.ticker,
                name   = state.asset?.name,
                onBack = onBack
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                }

                state.asset == null -> {
                    Box(
                        modifier         = Modifier.fillMaxSize().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = state.error ?: "Activo no encontrado",
                            fontSize = 14.sp,
                            color    = TextSecondary
                        )
                    }
                }

                else -> {
                    val isFixedIncome = FixedIncomeCategories.isFixedIncome(state.asset!!.assetCategoryId)

                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        // Tarjeta de cabecera con precio actual (solo activos no de renta fija)
                        if (!isFixedIncome) {
                            item {
                                AssetSummaryCard(
                                    ticker            = state.asset!!.ticker,
                                    name              = state.asset!!.name,
                                    currentPrice      = state.asset!!.currentPrice,
                                    currentPriceUpdatedAt = state.asset!!.currentPriceUpdatedAt,
                                    currencyCode      = state.currencyCode,
                                    onUpdatePrice     = { viewModel.openUpdatePriceSheet() },
                                    modifier          = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                )
                            }
                        }

                        // Fecha de vencimiento (solo renta fija)
                        if (isFixedIncome && state.asset!!.maturityDate != null) {
                            item {
                                MaturityDateCard(
                                    maturityDate = state.asset!!.maturityDate!!,
                                    isBond       = state.asset!!.isBond,
                                    modifier     = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                )
                            }
                        }

                        // Tarjeta de posición FIFO
                        state.position?.let { pos ->
                            item {
                                PositionCard(
                                    position       = pos,
                                    currencyCode   = state.currencyCode,
                                    balancesHidden = balancesHidden,
                                    modifier       = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Desglose FIFO (lotes vivos + cierres con trazabilidad)
                        state.breakdown?.takeIf { it.hasAnyData }?.let { bd ->
                            item {
                                FifoBreakdownSection(
                                    breakdown      = bd,
                                    currencyCode   = state.currencyCode,
                                    balancesHidden = balancesHidden,
                                    modifier       = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Cabecera de la lista
                        item {
                            Text(
                                text       = "MOVIMIENTOS",
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextSecondary,
                                modifier   = Modifier.padding(start = 24.dp, top = 18.dp, bottom = 8.dp)
                            )
                        }

                        if (state.transactionsDesc.isEmpty()) {
                            item { EmptyTransactionsCard() }
                        } else {
                            items(
                                items = state.transactionsDesc,
                                key   = { it.id }
                            ) { tx ->
                                TransactionRow(
                                    tx             = tx,
                                    platform       = state.platforms.firstOrNull { it.id == tx.platformId },
                                    currencyCode   = state.currencyCode,
                                    balancesHidden = balancesHidden,
                                    onEdit         = { viewModel.openEditSheet(tx) },
                                    onDelete       = { viewModel.requestDelete(tx) },
                                    modifier       = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Sección de dividendos
                        if (state.dividends.isNotEmpty()) {
                            item {
                                Text(
                                    text       = "DIVIDENDOS",
                                    fontSize   = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = TextSecondary,
                                    modifier   = Modifier.padding(start = 24.dp, top = 18.dp, bottom = 8.dp)
                                )
                            }
                            items(
                                items = state.dividends,
                                key   = { it.id }
                            ) { dividend ->
                                DividendRow(
                                    dividend       = dividend,
                                    currencyCode   = state.currencyCode,
                                    balancesHidden = balancesHidden,
                                    onDelete       = { viewModel.deleteDividend(dividend.linkedAssetTransactionId ?: dividend.id) },
                                    modifier       = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB con menú contextual según tipo de activo
        if (state.asset != null) {
            val isFixedIncome = FixedIncomeCategories.isFixedIncome(state.asset!!.assetCategoryId)
            val isBond = state.asset!!.isBond
            val isDeposit = state.asset!!.isDeposit

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 32.dp)
            ) {
                FloatingActionButton(
                    onClick        = { fabMenuOpen = true },
                    modifier       = Modifier.size(56.dp),
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
                        text        = { Text("Nuevo movimiento", color = TextPrimary) },
                        leadingIcon = { Text("💱", fontSize = 16.sp) },
                        onClick     = { fabMenuOpen = false; viewModel.openAddSheet() }
                    )
                    if (!isFixedIncome) {
                        DropdownMenuItem(
                            text        = { Text("Registrar dividendo", color = TextPrimary) },
                            leadingIcon = { Text("📈", fontSize = 16.sp) },
                            onClick     = { fabMenuOpen = false; viewModel.openDividendSheet() }
                        )
                    }
                    if (isBond) {
                        DropdownMenuItem(
                            text        = { Text("Registrar cupón", color = TextPrimary) },
                            leadingIcon = { Text("💰", fontSize = 16.sp) },
                            onClick     = { fabMenuOpen = false; viewModel.openBondDepositSheet() }
                        )
                    }
                    if (isDeposit) {
                        DropdownMenuItem(
                            text        = { Text("Registrar intereses", color = TextPrimary) },
                            leadingIcon = { Text("🏦", fontSize = 16.sp) },
                            onClick     = { fabMenuOpen = false; viewModel.openBondDepositSheet() }
                        )
                    }
                    if (state.isTransferable) {
                        DropdownMenuItem(
                            text        = { Text("Traspasar fondo", color = TextPrimary) },
                            leadingIcon = { Text("🔄", fontSize = 16.sp) },
                            onClick     = { fabMenuOpen = false; viewModel.openTransferSheet() }
                        )
                    }
                }
            }
        }
    }

    // ── Sheets y diálogos ───────────────────────────────────────────────────
    if (state.showAddSheet && state.asset != null) {
        AddEditAssetTransactionBottomSheet(
            transaction       = state.editing,
            fixedAsset        = state.asset,
            allAssets         = listOfNotNull(state.asset),
            platforms         = state.platforms,
            assetTransactions = state.transactionsAsc,
            currencyCode      = state.currencyCode,
            onSave            = { _, type, qty, price, date, platformId, feeNote, notes ->
                viewModel.saveTransaction(type, qty, price, date, platformId, feeNote, notes)
            },
            onCreatePlatform  = { /* desde la pantalla de historial no permitimos crear plataformas inline:
                                     redirigimos al usuario a Ajustes */ },
            onDismiss         = { viewModel.closeAddSheet() }
        )
    }

    if (state.showUpdatePriceSheet && state.asset != null) {
        UpdateCurrentPriceSheet(
            asset        = state.asset!!,
            currencyCode = state.currencyCode,
            onConfirm    = { newPrice -> viewModel.refreshCurrentPrice(newPrice) },
            onDismiss    = { viewModel.closeUpdatePriceSheet() }
        )
    }

    state.pendingDelete?.let { tx ->
        val isTransfer = tx.isTransfer
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor   = SurfaceWhite,
            icon             = { Text("⚠️", fontSize = 28.sp) },
            title = {
                Text(
                    if (isTransfer) "Eliminar traspaso" else "Eliminar movimiento",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary
                )
            },
            text = {
                Text(
                    text     = if (isTransfer)
                        "Se eliminarán ambas patas del traspaso (salida y entrada). " +
                        "El P&L de ambos fondos se recalculará. Esta acción no se puede deshacer."
                    else
                        "Se eliminará el movimiento del ${formatFullDate(tx.date)}. " +
                        "El P&L del activo se recalculará. Esta acción no se puede deshacer.",
                    fontSize = 14.sp,
                    color    = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

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

    // Sheet de dividendo
    if (state.showDividendSheet && state.asset != null) {
        AddDividendBottomSheet(
            fixedAssetName = state.asset!!.name,
            currencyCode   = state.currencyCode,
            onSave         = { _, grossAmount, irpfPercent, date ->
                viewModel.saveDividend(grossAmount, irpfPercent, date)
            },
            onDismiss      = { viewModel.closeDividendSheet() }
        )
    }

    // Sheet de bono/depósito
    if (state.showBondDepositSheet && state.asset != null) {
        AddBondDepositBottomSheet(
            fixedAssetName = state.asset!!.name,
            currencyCode   = state.currencyCode,
            onSave         = { _, grossAmount, irpfPercent, commission, date ->
                viewModel.saveBondDeposit(grossAmount, irpfPercent, commission, date)
            },
            onDismiss      = { viewModel.closeBondDepositSheet() }
        )
    }

    // Sheet de traspaso entre fondos
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
            onDismiss         = { viewModel.closeTransferSheet() }
        )
    }
}

// ─── Cabecera ────────────────────────────────────────────────────────────────

@Composable
private fun AssetHistoryHeader(
    ticker: String?,
    name: String?,
    onBack: () -> Unit
) {
    Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text       = ticker ?: "—",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary
                )
                if (name != null) {
                    Text(
                        text     = name,
                        fontSize = 12.sp,
                        color    = TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ─── Tarjeta cabecera del activo (precio actual + actualizar) ────────────────

@Composable
private fun AssetSummaryCard(
    ticker: String,
    name: String,
    currentPrice: Double?,
    currentPriceUpdatedAt: Long?,
    currencyCode: String,
    onUpdatePrice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    Card(
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
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = ticker.take(3),
                    fontSize   = if (ticker.length > 3) 10.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    textAlign  = TextAlign.Center
                )
            }
            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Precio actual", fontSize = 11.sp, color = TextSecondary)
                Spacer(Modifier.height(2.dp))
                if (currentPrice != null) {
                    Text(
                        text       = "${formatAmount(currentPrice)} $symbol",
                        fontSize   = 18.sp,
                        color      = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (currentPriceUpdatedAt != null) {
                        Text(
                            text     = "actualizado ${formatRelativeTime(currentPriceUpdatedAt)}",
                            fontSize = 10.sp,
                            color    = TextSecondary.copy(alpha = 0.75f)
                        )
                    }
                } else {
                    Text(
                        text       = "Sin precio registrado",
                        fontSize   = 14.sp,
                        color      = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Botón actualizar precio
            OutlinedButton(
                onClick        = onUpdatePrice,
                shape          = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint     = PrimaryDark
                )
                Spacer(Modifier.width(4.dp))
                Text("Actualizar", fontSize = 12.sp, color = PrimaryDark)
            }
        }
    }
}

// ─── Tarjeta de posición FIFO ────────────────────────────────────────────────

@Composable
private fun PositionCard(
    position: AssetPosition,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val isOpen = position.netQuantity > 0.0

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // ── Cantidad neta + coste medio ─────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricBlock(
                    label   = "Cantidad",
                    primary = if (isOpen) formatQty(position.netQuantity) else "—",
                    color   = Color.White
                )
                MetricBlock(
                    label     = "Coste medio",
                    primary   = if (isOpen)
                        "${maskAmount(formatAmount(position.averageCostOfRemaining), balancesHidden)} $symbol"
                    else "—",
                    color     = Color.White,
                    alignEnd  = true
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // ── Valor actual + invertido ───────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricBlock(
                    label   = "Invertido",
                    primary = if (isOpen)
                        "${maskAmount(formatAmount(position.totalInvestedRemaining), balancesHidden)} $symbol"
                    else "—",
                    color   = Color.White.copy(alpha = 0.85f)
                )
                MetricBlock(
                    label    = "Valor actual",
                    primary  = if (position.hasCurrentPrice && isOpen)
                        "${maskAmount(formatAmount(position.currentValue), balancesHidden)} $symbol"
                    else if (!position.hasCurrentPrice && isOpen) "Sin precio"
                    else "—",
                    color    = Color.White,
                    alignEnd = true
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))

            // ── P&L total destacado ────────────────────────────────────────
            val pnlColor = when {
                position.totalPnL > 0  -> Color(0xFF66BB6A)
                position.totalPnL < 0  -> Color(0xFFEF9A9A)
                else                   -> Color.White
            }
            Text("Beneficio total", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text       = if (position.totalPnL == 0.0) "—"
                                 else "${if (position.totalPnL >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(position.totalPnL)), balancesHidden)} $symbol",
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = pnlColor
                )
                Spacer(Modifier.width(10.dp))
                if (position.totalPnL != 0.0 && (isOpen || position.realizedPnL != 0.0)) {
                    Text(
                        text       = "${if (position.totalPnLPercent >= 0) "+" else "−"}${formatPercent1(abs(position.totalPnLPercent))}%",
                        fontSize   = 13.sp,
                        color      = pnlColor.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            // ── Desglose realizado / no realizado ─────────────────────────
            if (position.realizedPnL != 0.0 || position.unrealizedPnL != 0.0 || position.dividendIncome != 0.0) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PnLChip(label = "Realizado", amount = position.realizedPnL, symbol = symbol, masked = balancesHidden)
                    if (position.dividendIncome != 0.0) {
                        PnLChip(label = "Dividendos", amount = position.dividendIncome, symbol = symbol, masked = balancesHidden)
                    }
                    PnLChip(label = "Latente", amount = position.unrealizedPnL, symbol = symbol, masked = balancesHidden, unavailable = !position.hasCurrentPrice && isOpen)
                }
            }
        }
    }
}

@Composable
private fun MetricBlock(
    label: String,
    primary: String,
    color: Color,
    alignEnd: Boolean = false
) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
        Spacer(Modifier.height(3.dp))
        Text(
            text       = primary,
            fontSize   = 15.sp,
            color      = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PnLChip(
    label: String,
    amount: Double,
    symbol: String,
    masked: Boolean,
    unavailable: Boolean = false
) {
    val text = when {
        unavailable    -> "Sin precio"
        amount == 0.0  -> "—"
        else           -> "${if (amount >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(amount)), masked)} $symbol"
    }
    val color = when {
        unavailable    -> Color.White.copy(alpha = 0.55f)
        amount > 0     -> Color(0xFF66BB6A)
        amount < 0     -> Color(0xFFEF9A9A)
        else           -> Color.White.copy(alpha = 0.65f)
    }
    Column {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.55f))
        Spacer(Modifier.height(2.dp))
        Text(text, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// ─── Item de movimiento ──────────────────────────────────────────────────────

@Composable
private fun TransactionRow(
    tx: AssetTransaction,
    platform: Platform?,
    currencyCode: String,
    balancesHidden: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol  = currencySymbol(currencyCode)
    val isBuy   = tx.type == AssetTransactionType.BUY
    val isTransferOut = tx.type == AssetTransactionType.TRANSFER_OUT
    val isTransferIn  = tx.type == AssetTransactionType.TRANSFER_IN
    val sideColor = when {
        isBuy || isTransferIn  -> IncomeGreen
        else                   -> ExpenseRed
    }
    val sideLabel = when (tx.type) {
        AssetTransactionType.BUY          -> "Compra"
        AssetTransactionType.SELL         -> "Venta"
        AssetTransactionType.TRANSFER_OUT -> "Traspaso salida"
        AssetTransactionType.TRANSFER_IN  -> "Traspaso entrada"
    }
    val sign = when {
        isBuy || isTransferIn  -> "+"
        else                   -> "−"
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
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono lateral con tipo
            Box(
                modifier         = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(sideColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = sideIcon,
                    fontSize   = 18.sp,
                    color      = sideColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))

            // Info principal
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = sideLabel,
                        fontSize   = 13.sp,
                        color      = sideColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text     = "${formatQty(tx.quantity)} × ${maskAmount(formatAmount(tx.pricePerUnit), balancesHidden)} $symbol",
                        fontSize = 12.sp,
                        color    = TextPrimary
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text     = formatShortDate(tx.date),
                        fontSize = 11.sp,
                        color    = TextSecondary
                    )
                    if (platform != null) {
                        Text("  ·  ", fontSize = 11.sp, color = TextSecondary)
                        Text(platform.icon, fontSize = 12.sp)
                        Spacer(Modifier.width(3.dp))
                        Text(platform.name, fontSize = 11.sp, color = TextSecondary)
                    }
                }
                if (!tx.feeNote.isNullOrBlank()) {
                    Text(
                        text     = "Comisión: ${tx.feeNote}",
                        fontSize = 10.sp,
                        color    = TextSecondary.copy(alpha = 0.8f)
                    )
                }
                if (!tx.notes.isNullOrBlank()) {
                    Text(
                        text     = tx.notes,
                        fontSize = 10.sp,
                        color    = TextSecondary.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            // Importe bruto
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "$sign ${maskAmount(formatAmount(tx.grossAmount), balancesHidden)} $symbol",
                    fontSize   = 13.sp,
                    color      = sideColor,
                    fontWeight = FontWeight.SemiBold
                )
                Row {
                    if (!tx.isTransfer) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp), tint = TextSecondary)
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(13.dp), tint = ExpenseRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTransactionsCard() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceWhite)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📋", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "Sin movimientos",
                fontSize   = 14.sp,
                color      = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Pulsa + para registrar tu primera compra",
                fontSize  = 12.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Helpers de formato locales ──────────────────────────────────────────────

// ─── Desglose FIFO ──────────────────────────────────────────────────────────────────────

/**
 * Tarjeta colapsable que muestra el detalle FIFO de un activo:
 * los lotes vivos (compras aún no consumidas) y, para cada venta, los
 * lotes contra los que se cruzó con su P&L parcial. Aparece después de
 * la [PositionCard] siempre que haya algo que mostrar (al menos un lote
 * vivo o una venta).
 */
@Composable
private fun FifoBreakdownSection(
    breakdown: FifoBreakdown,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val symbol = currencySymbol(currencyCode)

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabecera con toggle
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🧾", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text       = "Desglose FIFO",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextPrimary
                        )
                        val openCount = breakdown.openLots.size
                        val saleCount = breakdown.saleMatches.size
                        val subtitle = buildString {
                            if (openCount > 0) {
                                append("$openCount ")
                                append(if (openCount == 1) "lote en cartera" else "lotes en cartera")
                            }
                            if (openCount > 0 && saleCount > 0) append("  ·  ")
                            if (saleCount > 0) {
                                append("$saleCount ")
                                append(if (saleCount == 1) "cierre" else "cierres")
                            }
                        }
                        Text(subtitle, fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint     = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.12f), thickness = 0.5.dp)

                    // Lotes en cartera
                    if (breakdown.openLots.isNotEmpty()) {
                        FifoSubHeader(text = "Lotes en cartera")
                        breakdown.openLots.forEachIndexed { idx, lot ->
                            FifoOpenLotRow(
                                index  = idx + 1,
                                lot    = lot,
                                symbol = symbol,
                                masked = balancesHidden
                            )
                        }
                    }

                    // Cierres FIFO
                    if (breakdown.saleMatches.isNotEmpty()) {
                        if (breakdown.openLots.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(
                                modifier  = Modifier.padding(horizontal = 16.dp),
                                color     = TextSecondary.copy(alpha = 0.10f),
                                thickness = 0.5.dp
                            )
                        }
                        FifoSubHeader(text = "Cierres FIFO")
                        breakdown.saleMatches.forEach { sale ->
                            FifoSaleMatchBlock(
                                sale   = sale,
                                symbol = symbol,
                                masked = balancesHidden
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FifoSubHeader(text: String) {
    Text(
        text       = text.uppercase(),
        fontSize   = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color      = TextSecondary,
        modifier   = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
    )
}

@Composable
private fun FifoOpenLotRow(
    index: Int,
    lot: FifoOpenLot,
    symbol: String,
    masked: Boolean
) {
    val partial = lot.remainingQuantity < lot.originalQuantity
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(IncomeGreen.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "#$index",
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold,
                color      = IncomeGreen
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = "${formatQty(lot.remainingQuantity)} u. × ${maskAmount(formatAmount(lot.pricePerUnit), masked)} $symbol",
                fontSize   = 13.sp,
                color      = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            val datePart = formatShortDate(lot.purchaseDate)
            val partialNote = if (partial)
                "  ·  ${formatQty(lot.remainingQuantity)} de ${formatQty(lot.originalQuantity)} restantes"
            else ""
            Text(
                text     = "Comprado el $datePart$partialNote",
                fontSize = 11.sp,
                color    = TextSecondary
            )
        }
        Text(
            text       = "${maskAmount(formatAmount(lot.remainingCost), masked)} $symbol",
            fontSize   = 12.sp,
            color      = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FifoSaleMatchBlock(
    sale: FifoSaleMatch,
    symbol: String,
    masked: Boolean
) {
    val pnlColor = when {
        sale.realizedPnL > 0 -> IncomeGreen
        sale.realizedPnL < 0 -> ExpenseRed
        else                 -> TextSecondary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundGray.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Cabecera de la venta
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("↘", fontSize = 14.sp, color = ExpenseRed, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = "Venta de ${formatQty(sale.saleQuantity)} u.",
                        fontSize   = 13.sp,
                        color      = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text     = "${formatShortDate(sale.saleDate)}  ·  ${maskAmount(formatAmount(sale.salePrice), masked)} $symbol/u.",
                    fontSize = 11.sp,
                    color    = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("P&L", fontSize = 9.sp, color = TextSecondary)
                Text(
                    text       = if (sale.realizedPnL == 0.0) "—"
                                 else "${if (sale.realizedPnL >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(sale.realizedPnL)), masked)} $symbol",
                    fontSize   = 13.sp,
                    color      = pnlColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Lotes consumidos
        if (sale.consumed.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            sale.consumed.forEach { c ->
                val cColor = when {
                    c.pnl > 0 -> IncomeGreen
                    c.pnl < 0 -> ExpenseRed
                    else      -> TextSecondary
                }
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, top = 3.dp, bottom = 3.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text     = "↳ ${formatQty(c.quantityConsumed)} u. del lote del ${formatShortDate(c.purchaseDate)}",
                            fontSize = 11.sp,
                            color    = TextPrimary
                        )
                        Text(
                            text     = "compra a ${maskAmount(formatAmount(c.purchasePrice), masked)} $symbol/u.",
                            fontSize = 10.sp,
                            color    = TextSecondary
                        )
                    }
                    Text(
                        text       = if (c.pnl == 0.0) "—"
                                     else "${if (c.pnl >= 0) "+" else "−"} ${maskAmount(formatAmount(abs(c.pnl)), masked)} $symbol",
                        fontSize   = 11.sp,
                        color      = cColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatPercent1(value: Double): String {
    val rounded = (value * 10).toLong()
    return "${rounded / 10},${rounded % 10}"
}

@Composable
private fun MaturityDateCard(
    maturityDate: Long,
    isBond: Boolean,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now().toEpochMilliseconds()
    val isExpired = maturityDate < now
    val label = if (isBond) "Vencimiento del bono" else "Vencimiento del depósito"

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isExpired) ExpenseRed.copy(alpha = 0.08f) else SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isExpired) "⏰" else "📅",
                fontSize = 24.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = label,
                    fontSize   = 11.sp,
                    color      = TextSecondary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = formatFullDate(maturityDate),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isExpired) ExpenseRed else TextPrimary
                )
                if (isExpired) {
                    Text(
                        text     = "Vencido",
                        fontSize = 11.sp,
                        color    = ExpenseRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatShortDate(epochMillis: Long): String {
    val months = listOf("ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic")
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val ld: LocalDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} ${months[ld.monthNumber - 1]} ${ld.year}"
}

private fun formatFullDate(epochMillis: Long): String {
    val months = listOf("enero","febrero","marzo","abril","mayo","junio",
        "julio","agosto","septiembre","octubre","noviembre","diciembre")
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val ld: LocalDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}

// ─── Fila de dividendo ────────────────────────────────────────────────────────

@Composable
private fun DividendRow(
    dividend: Transaction,
    currencyCode: String,
    balancesHidden: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val gross = dividend.grossAmount ?: dividend.amount
    val irpf = if (dividend.grossAmount != null && dividend.irpfPercent != null)
        dividend.grossAmount * dividend.irpfPercent / 100.0 else 0.0
    val net = dividend.amount

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                modifier         = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(IncomeGreen.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📈", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Dividendo",
                    fontSize   = 13.sp,
                    color      = IncomeGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = formatShortDate(dividend.date),
                    fontSize = 11.sp,
                    color    = TextSecondary
                )
                if (irpf > 0) {
                    Text(
                        text     = "Bruto: ${maskAmount(formatAmount(gross), balancesHidden)} $symbol \u00b7 IRPF: ${maskAmount(formatAmount(irpf), balancesHidden)} $symbol",
                        fontSize = 10.sp,
                        color    = TextSecondary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.width(6.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "+ ${maskAmount(formatAmount(net), balancesHidden)} $symbol",
                    fontSize   = 13.sp,
                    color      = IncomeGreen,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(13.dp), tint = ExpenseRed)
                }
            }
        }
    }
}
