package es.aviferdev.trackfolio.ui.debt

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebtListScreen(viewModel: DebtViewModel = koinViewModel()) {
    val uiState        by viewModel.uiState.collectAsState()
    val balancesHidden  = LocalBalanceHidden.current
    var showAdd        by remember { mutableStateOf(false) }
    var debtToEdit     by remember { mutableStateOf<Debt?>(null) }
    var debtToMarkPaid by remember { mutableStateOf<Debt?>(null) }
    var debtToDelete   by remember { mutableStateOf<Debt?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color    = PrimaryDark
            )
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // ── Header ────────────────────────────────────────────────────
                item { DebtTopBar(uiState.totalTheyOwe, uiState.totalIOwe, balancesHidden) }

                // ── Me deben ──────────────────────────────────────────────────
                if (uiState.debtsTheyOwe.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title  = "Me deben",
                            total  = uiState.totalTheyOwe,
                            color  = IncomeGreen,
                            hidden = balancesHidden
                        )
                    }
                    items(uiState.debtsTheyOwe, key = { it.id }) { debt ->
                        SwipeDebt(onDelete = { debtToDelete = debt }) {
                            DebtRow(
                                debt       = debt,
                                hidden     = balancesHidden,
                                onMarkPaid = { debtToMarkPaid = debt },
                                onEdit     = { debtToEdit = debt }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = BorderGray, thickness = .5.dp)
                    }
                }

                // ── Debo yo ───────────────────────────────────────────────────
                if (uiState.debtsIOwe.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title  = "Debo yo",
                            total  = uiState.totalIOwe,
                            color  = ExpenseRed,
                            hidden = balancesHidden
                        )
                    }
                    items(uiState.debtsIOwe, key = { it.id }) { debt ->
                        SwipeDebt(onDelete = { debtToDelete = debt }) {
                            DebtRow(
                                debt       = debt,
                                hidden     = balancesHidden,
                                onMarkPaid = { debtToMarkPaid = debt },
                                onEdit     = { debtToEdit = debt }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = BorderGray, thickness = .5.dp)
                    }
                }

                if (uiState.debtsTheyOwe.isEmpty() && uiState.debtsIOwe.isEmpty()) {
                    item { EmptyState() }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick        = { showAdd = true },
            modifier       = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp)
                .size(52.dp),
            shape          = RoundedCornerShape(16.dp),
            containerColor = PrimaryDark,
            contentColor   = Color.White,
            elevation      = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = Color.White)
        }
    }

    // ── Sheets ────────────────────────────────────────────────────────────────
    if (showAdd) {
        AddDebtBottomSheet(onDismiss = { showAdd = false }, viewModel = viewModel)
    }
    debtToEdit?.let { debt ->
        AddDebtBottomSheet(editingDebt = debt, onDismiss = { debtToEdit = null }, viewModel = viewModel)
    }
    debtToDelete?.let { debt ->
        DeleteDebtDialog(
            personName = debt.personName,
            onConfirm  = { viewModel.deleteDebt(debt.id); debtToDelete = null },
            onDismiss  = { debtToDelete = null }
        )
    }
    debtToMarkPaid?.let { debt ->
        MarkPaidDialog(
            personName = debt.personName,
            amount     = debt.amount,
            onConfirm  = { viewModel.markAsPaid(debt.id); debtToMarkPaid = null },
            onDismiss  = { debtToMarkPaid = null }
        )
    }
}

// ─── Top bar with summary card ────────────────────────────────────────────────
@Composable
private fun DebtTopBar(totalTheyOwe: Double, totalIOwe: Double, hidden: Boolean) {
    Surface(color = SurfaceWhite, shadowElevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 16.dp)
        ) {
            Text(
                "Deudas",
                fontSize      = 18.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPrimary,
                letterSpacing = (-.3).sp
            )
            Spacer(Modifier.height(14.dp))

            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = PrimaryDark),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    DebtSummaryCell(
                        label    = "Me deben",
                        amount   = totalTheyOwe,
                        color    = Color(0xFF86EFAC),
                        hidden   = hidden,
                        modifier = Modifier.weight(1f),
                        alignEnd = false
                    )
                    Box(
                        Modifier
                            .width(.5.dp)
                            .height(44.dp)
                            .background(Color.White.copy(.12f))
                            .align(Alignment.CenterVertically)
                    )
                    DebtSummaryCell(
                        label    = "Debo yo",
                        amount   = totalIOwe,
                        color    = Color(0xFFFCA5A5),
                        hidden   = hidden,
                        modifier = Modifier.weight(1f),
                        alignEnd = true
                    )
                }
            }
        }
    }
    HorizontalDivider(color = BorderGray, thickness = .5.dp)
}

@Composable
private fun DebtSummaryCell(
    label: String,
    amount: Double,
    color: Color,
    hidden: Boolean,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Column(
        modifier            = modifier.padding(horizontal = 10.dp),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(.5f))
        Spacer(Modifier.height(4.dp))
        Text(
            "${maskAmount(formatAmount(amount), hidden)} €",
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}

// ─── Section header ───────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, total: Double, color: Color, hidden: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 18.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(color))
            Spacer(Modifier.width(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
        Text(
            "${maskAmount(formatAmount(total), hidden)} €",
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}

// ─── Debt row ─────────────────────────────────────────────────────────────────
@Composable
private fun DebtRow(
    debt: Debt,
    hidden: Boolean,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit
) {
    val isTheyOwe   = debt.direction == DebtDirection.THEY_OWE
    val avatarColor = if (isTheyOwe) IncomeGreen else ExpenseRed
    val amountColor = if (isTheyOwe) IncomeGreen else ExpenseRed
    val prefix      = if (isTheyOwe) "+" else "−"
    val initial     = debt.personName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(initial, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.width(12.dp))

        // Name + notes
        Column(modifier = Modifier.weight(1f)) {
            Text(debt.personName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            if (!debt.notes.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(debt.notes, fontSize = 11.sp, color = TextTertiary)
            }
        }

        // Amount + actions
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$prefix ${maskAmount(formatAmount(debt.amount), hidden)} €",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = amountColor
            )
            Spacer(Modifier.height(4.dp))
            Row {
                TextButton(
                    onClick        = onMarkPaid,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier       = Modifier.height(22.dp)
                ) {
                    Text("Pagada", fontSize = 10.sp, color = PrimaryDark, fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick        = onEdit,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier       = Modifier.height(22.dp)
                ) {
                    Text("Editar", fontSize = 10.sp, color = TextTertiary)
                }
            }
        }
    }
}

// ─── Swipe to delete ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeDebt(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { v -> if (v == SwipeToDismissBoxValue.EndToStart) { onDelete(); false } else false }
    )
    SwipeToDismissBox(
        state                       = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val bg by animateColorAsState(
                targetValue = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) ExpenseRed else Color.Transparent,
                label       = "swipe_debt"
            )
            Box(
                modifier         = Modifier.fillMaxSize().background(bg).padding(end = 18.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Eliminar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    ) {
        Surface(color = SurfaceWhite) { content() }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────
@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sin deudas activas", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Pulsa + para registrar una deuda nueva", fontSize = 13.sp, color = TextTertiary, textAlign = TextAlign.Center)
        }
    }
}

// ─── Dialogs ─────────────────────────────────────────────────────────────────
@Composable
private fun MarkPaidDialog(personName: String, amount: Double, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceWhite,
        title = { Text("Marcar como pagada", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text  = { Text("¿Confirmas que la deuda de ${formatAmount(amount)} € con $personName ha sido saldada?", fontSize = 13.sp, color = TextSecondary) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Confirmar", color = IncomeGreen, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = PrimaryDark) } },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DeleteDebtDialog(personName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceWhite,
        title = { Text("Eliminar deuda", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text  = { Text("¿Seguro que quieres eliminar la deuda con $personName? Esta acción no se puede deshacer.", fontSize = 13.sp, color = TextSecondary) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = PrimaryDark) } },
        shape = RoundedCornerShape(16.dp)
    )
}

// ─── Local format helper (keeps the file self-contained) ─────────────────────
private fun formatAmount(amount: Double): String {
    val rounded  = (amount * 100).toLong()
    val euros    = rounded / 100
    val cents    = rounded % 100
    val eurosStr = euros.toString().reversed()
        .chunked(3).joinToString(".").reversed()
    return "$eurosStr,${cents.toString().padStart(2,'0')}"
}
