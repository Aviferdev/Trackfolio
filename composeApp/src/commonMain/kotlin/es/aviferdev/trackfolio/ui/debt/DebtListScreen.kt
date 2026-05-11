package es.aviferdev.trackfolio.ui.debt

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.ui.common.*
import es.aviferdev.trackfolio.ui.theme.BackgroundGray
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.LocalBalanceHidden
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.formatDate
import es.aviferdev.trackfolio.ui.theme.maskAmount
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.ui.tooling.preview.Preview
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
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryDark)
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
                        DebtSectionHeader(title = "Me deben ↑", total = uiState.totalTheyOwe, color = IncomeGreen, hidden = balancesHidden)
                    }
                    items(uiState.debtsTheyOwe, key = { it.id }) { debt ->
                        SwipeDebt(onDelete = { debtToDelete = debt }) {
                            DebtCard(debt = debt, hidden = balancesHidden, onMarkPaid = { debtToMarkPaid = debt }, onEdit = { debtToEdit = debt })
                        }
                    }
                }

                // ── Debo yo ───────────────────────────────────────────────────
                if (uiState.debtsIOwe.isNotEmpty()) {
                    item {
                        DebtSectionHeader(title = "Debo yo ↓", total = uiState.totalIOwe, color = ExpenseRed, hidden = balancesHidden)
                    }
                    items(uiState.debtsIOwe, key = { it.id }) { debt ->
                        SwipeDebt(onDelete = { debtToDelete = debt }) {
                            DebtCard(debt = debt, hidden = balancesHidden, onMarkPaid = { debtToMarkPaid = debt }, onEdit = { debtToEdit = debt })
                        }
                    }
                }

                if (uiState.debtsTheyOwe.isEmpty() && uiState.debtsIOwe.isEmpty()) {
                    item { EmptyState() }
                }

                // ── Full-width add button (estilo JSX) ─────────────────────────
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick        = { showAdd = true },
                        modifier       = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape          = RoundedCornerShape(12.dp),
                        colors         = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                        contentPadding = PaddingValues(vertical = 13.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Nueva deuda", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // ── Sheets ────────────────────────────────────────────────────────────────
    if (showAdd) { AddDebtBottomSheet(onDismiss = { showAdd = false }, viewModel = viewModel) }
    debtToEdit?.let { AddDebtBottomSheet(editingDebt = it, onDismiss = { debtToEdit = null }, viewModel = viewModel) }
    debtToDelete?.let {
        AlertDialog(onDismissRequest = { debtToDelete = null }, containerColor = SurfaceWhite,
            title = { Text("Eliminar deuda", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text  = { Text("¿Eliminar deuda con ${it.personName}?", fontSize = 13.sp, color = TextSecondary) },
            confirmButton = { TextButton(onClick = { viewModel.deleteDebt(it.id); debtToDelete = null }) { Text("Eliminar", color = ExpenseRed) } },
            dismissButton = { TextButton(onClick = { debtToDelete = null }) { Text("Cancelar", color = PrimaryDark) } },
            shape = RoundedCornerShape(16.dp)
        )
    }
    debtToMarkPaid?.let {
        AlertDialog(onDismissRequest = { debtToMarkPaid = null }, containerColor = SurfaceWhite,
            title = { Text("Marcar como pagado", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text  = { Text("¿Marcar como pagado a ${it.personName}?", fontSize = 13.sp, color = TextSecondary) },
            confirmButton = { TextButton(onClick = { viewModel.markAsPaid(it.id); debtToMarkPaid = null }) { Text("Sí, pagado", color = IncomeGreen) } },
            dismissButton = { TextButton(onClick = { debtToMarkPaid = null }) { Text("Cancelar", color = PrimaryDark) } },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ─── Top bar with summary card ────────────────────────────────────────────────
@Composable
private fun DebtTopBar(totalTheyOwe: Double, totalIOwe: Double, hidden: Boolean) {
    Surface(color = SurfaceWhite, shadowElevation = 0.dp) {
        Column(modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 16.dp).padding(top = 14.dp, bottom = 16.dp)) {
            Text("Deudas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
            Spacer(Modifier.height(14.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = PrimaryDark), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
                    DebtSummaryCell(label = "Me deben", amount = totalTheyOwe, color = Color(0xFFB4FFB4), hidden = hidden, modifier = Modifier.weight(1f), alignEnd = false)
                    Box(Modifier.width(1.dp).height(44.dp).background(Color.White.copy(alpha = 0.18f)).align(Alignment.CenterVertically))
                    DebtSummaryCell(label = "Debo yo", amount = totalIOwe, color = Color(0xFFFFB4B4), hidden = hidden, modifier = Modifier.weight(1f), alignEnd = true)
                }
            }
        }
    }
    HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
}

@Composable
private fun DebtSummaryCell(label: String, amount: Double, color: Color, hidden: Boolean, modifier: Modifier, alignEnd: Boolean) {
    Column(modifier = modifier.padding(horizontal = 10.dp), horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        TrackfolioLabel(text = label, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 4.dp))
        Text(
            "${maskAmount(formatAmount(amount), hidden)} €",
            fontSize   = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = color
        )
    }
}

// ─── Section header ───────────────────────────────────────────────────────────
@Composable
private fun DebtSectionHeader(title: String, total: Double, color: Color, hidden: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        TrackfolioLabel(text = title)
        Text("Total: ${maskAmount(formatAmount(total), hidden)} €", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(end = 4.dp))
    }
}

// ─── Debt card ────────────────────────────────────────────────────────────────
@Composable
private fun DebtCard(debt: Debt, hidden: Boolean, onMarkPaid: () -> Unit, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            InitialsAvatar(text = debt.personName.firstOrNull()?.uppercase() ?: "?", bgColor = if (debt.direction == DebtDirection.THEY_OWE) IncomeGreen else ExpenseRed, size = 38.dp, textSize = 15)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(debt.personName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("${debt.notes ?: "Sin nota"} · ${formatDate(debt.date)}", fontSize = 11.sp, color = TextTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${maskAmount(formatAmount(debt.amount), hidden)} €", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (debt.direction == DebtDirection.THEY_OWE) IncomeGreen else ExpenseRed)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = onMarkPaid, colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen.copy(alpha = 0.15f), contentColor = IncomeGreen), contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp), modifier = Modifier.height(24.dp)) {
                        Text("Pagado", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onEdit, colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary), modifier = Modifier.height(24.dp)) {
                        Text("Editar", fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// ─── Swipe to delete ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeDebt(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(confirmValueChange = { v -> if (v == SwipeToDismissBoxValue.EndToStart) { onDelete(); false } else false })
    SwipeToDismissBox(state = state, enableDismissFromStartToEnd = false, enableDismissFromEndToStart = true,
        backgroundContent = {
            val bg by animateColorAsState(targetValue = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) ExpenseRed else Color.Transparent, label = "swipe")
            Box(modifier = Modifier.fillMaxSize().background(bg).padding(end = 18.dp), contentAlignment = Alignment.CenterEnd) { Text("Eliminar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
        }
    ) { Surface(color = SurfaceWhite) { content() } }
}

// ─── Empty state ───────────────────────────────────────────────────────────────
@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🤝", fontSize = 44.sp)
            Spacer(Modifier.height(14.dp))
            Text("Sin deudas", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Pulsa \"Nueva deuda\" para registrar\nuna deuda pendiente", fontSize = 13.sp, color = TextTertiary, textAlign = TextAlign.Center)
        }
    }
}

private fun createMockDebts(): Pair<List<Debt>, List<Debt>> {
    val now = Clock.System.now().toEpochMilliseconds()
    val dayInMillis = 24 * 60 * 60 * 1000L
    val theyOwe = listOf(
        Debt(id = "1", accountId = "acc1", personName = "Juan", amount = 150.0, direction = DebtDirection.THEY_OWE, date = now - (5 * dayInMillis), isPaid = false, notes = null, createdAt = now - (5 * dayInMillis)),
        Debt(id = "2", accountId = "acc1", personName = "María", amount = 75.50, direction = DebtDirection.THEY_OWE, date = now - (10 * dayInMillis), isPaid = false, notes = "Cena", createdAt = now - (10 * dayInMillis))
    )
    val iOwe = listOf(
        Debt(id = "3", accountId = "acc1", personName = "Carlos", amount = 200.0, direction = DebtDirection.I_OWE, date = now - (3 * dayInMillis), isPaid = false, notes = null, createdAt = now - (3 * dayInMillis))
    )
    return theyOwe to iOwe
}

