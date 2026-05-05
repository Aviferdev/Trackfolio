package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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

@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel()
) {
    val state               by viewModel.portfolioState.collectAsState()
    val sheetState          by viewModel.uiState.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()

    accountViewModel.selectAccount()

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
                    totalInvested     = state.totalInvested,
                    totalCurrentValue = state.totalCurrentValue,
                    totalPnL          = state.totalPnL,
                    totalPnLPercent   = state.totalPnLPercent,
                    positionsCount    = state.rows.size,
                    modifier          = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // ── Lista de grupos ──────────────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryDark)
                    }
                }
            } else if (state.groups.isEmpty()) {
                item { EmptyPortfolioState() }
            } else {
                state.groups.forEach { group ->
                    item(key = "header_${group.category?.id ?: "none"}") {
                        CategoryGroupHeader(group = group)
                    }
                    items(
                        items = group.rows,
                        key   = { row -> row.asset.id }
                    ) { row ->
                        AssetCard(
                            row      = row,
                            onEdit   = { viewModel.openEditSheet(row.asset) },
                            onDelete = { viewModel.requestDelete(row.asset) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick   = { viewModel.openAddSheet() },
            modifier  = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 32.dp)
                .size(56.dp),
            shape          = CircleShape,
            containerColor = PrimaryDark,
            contentColor   = Color.White,
            elevation      = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light, color = Color.White)
        }
    }

    // ── Sheets y diálogos ────────────────────────────────────────────────────
    if (sheetState.showAddSheet) {
        AddEditAssetBottomSheet(
            asset      = null,
            categories = availableCategories,
            onSave     = { ticker, name, qty, price, date, notes, categoryId ->
                viewModel.addAsset(ticker, name, qty, price, date, notes, categoryId)
            },
            onDismiss = { viewModel.closeAddSheet() }
        )
    }

    if (sheetState.showEditSheet && sheetState.editingAsset != null) {
        AddEditAssetBottomSheet(
            asset      = sheetState.editingAsset,
            categories = availableCategories,
            onSave     = { ticker, name, qty, price, date, notes, categoryId ->
                viewModel.editAsset(
                    sheetState.editingAsset!!,
                    ticker, name, qty, price, date, notes, categoryId
                )
            },
            onDismiss = { viewModel.closeEditSheet() }
        )
    }

    if (sheetState.showDeleteConfirm && sheetState.assetToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor   = SurfaceWhite,
            icon             = { Text("⚠️", fontSize = 28.sp) },
            title = {
                Text(
                    "Eliminar posición",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary
                )
            },
            text = {
                Text(
                    "Se eliminará la posición en ${sheetState.assetToDelete!!.name}. Esta acción no se puede deshacer.",
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
    positionsCount: Int,
    modifier: Modifier = Modifier
) {
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
            Text(
                text     = "Valor total",
                fontSize = 13.sp,
                color    = Color.White.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text          = formatAmount(totalCurrentValue),
                fontSize      = 34.sp,
                fontWeight    = FontWeight.Bold,
                color         = Color.White,
                letterSpacing = (-0.5).sp
            )

            if (totalPnL != 0.0) {
                Spacer(Modifier.height(8.dp))
                val pnlColor  = if (totalPnL >= 0) Color(0xFF66BB6A) else Color(0xFFEF9A9A)
                val pnlPrefix = if (totalPnL >= 0) "+" else "−"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = "$pnlPrefix ${formatAmount(abs(totalPnL))}",
                        fontSize   = 15.sp,
                        color      = pnlColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(pnlColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = "$pnlPrefix${formatPercent(abs(totalPnLPercent))}%",
                            fontSize   = 12.sp,
                            color      = pnlColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(label = "Invertido",  value = formatAmount(totalInvested))
                SummaryItem(label = "Posiciones", value = positionsCount.toString())
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

// ─── Cabecera de grupo de categoría ──────────────────────────────────────────
@Composable
private fun CategoryGroupHeader(group: CategoryGroup) {
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
            // Icono + nombre + nº de activos
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
                    text  = "(${group.rows.size})",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        // Línea con valores: invertido | actual
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            ValueChip(label = "Invertido", value = formatAmount(group.totalInvested))
            ValueChip(label = "Actual",    value = formatAmount(group.totalCurrentValue))
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

// ─── Tarjeta de activo ────────────────────────────────────────────────────────
@Composable
private fun AssetCard(
    row: AssetRow,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val asset      = row.asset
    val isPositive = row.pnlAmount >= 0
    val pnlColor   = if (row.pnlAmount > 0) IncomeGreen
                     else if (row.pnlAmount < 0) ExpenseRed
                     else TextSecondary

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

            // Nombre + cantidad
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
                    text     = "${formatQty(asset.quantity)} × ${formatAmount(asset.purchasePrice)}",
                    fontSize = 12.sp,
                    color    = TextSecondary
                )
            }

            Spacer(Modifier.width(8.dp))

            // Valor + P&L
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = formatAmount(row.currentValue),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary
                )
                if (row.pnlAmount != 0.0) {
                    val prefix = if (isPositive) "+" else "−"
                    Text(
                        text     = "$prefix ${formatAmount(abs(row.pnlAmount))}",
                        fontSize = 12.sp,
                        color    = pnlColor
                    )
                } else {
                    Text(
                        text     = "Sin precio actual",
                        fontSize = 11.sp,
                        color    = TextSecondary.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // Acciones
            Column {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp), tint = ExpenseRed)
                }
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
                "Pulsa + para registrar\ntu primera inversión",
                fontSize  = 14.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Helpers de formato ──────────────────────────────────────────────────────
private fun formatQty(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        val rounded = (value * 10000).toLong()
        val intPart = rounded / 10000
        val decPart = rounded % 10000
        "$intPart,${decPart.toString().trimEnd('0').padStart(1, '0')}"
    }
}

private fun formatPercent(value: Double): String {
    val rounded = (value * 10).toLong()
    return "${rounded / 10},${rounded % 10}"
}
