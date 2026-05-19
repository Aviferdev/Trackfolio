package es.aviferdev.n3to.ui.debt

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Debt
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.ui.common.InitialsAvatar
import es.aviferdev.n3to.ui.common.N3toLabel
import es.aviferdev.n3to.ui.common.button.LargeButtonApp
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.row.SwipeRowApp
import es.aviferdev.n3to.ui.common.separator.SpacerVerticalApp

import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.PrimaryDark

import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatDate
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.debt_add_title
import n3to.composeapp.generated.resources.debt_i_owe
import n3to.composeapp.generated.resources.debt_mark_paid
import n3to.composeapp.generated.resources.debt_no_debts
import n3to.composeapp.generated.resources.debt_paid
import n3to.composeapp.generated.resources.debt_they_owe
import n3to.composeapp.generated.resources.debt_title
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// WRAPPER: Conoce al ViewModel y Koin. Orquesta estado y delega al Content.
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun DebtListScreen(viewModel: DebtViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val balancesHidden = LocalBalanceHidden.current
    var showAdd by remember { mutableStateOf(false) }
    var debtToEdit by remember { mutableStateOf<Debt?>(null) }
    var debtToMarkPaid by remember { mutableStateOf<Debt?>(null) }
    var debtToDelete by remember { mutableStateOf<Debt?>(null) }

    DebtListContent(
        uiState = uiState,
        balancesHidden = balancesHidden,
        onAddClick = { showAdd = true },
        onEditDebt = { debtToEdit = it },
        onMarkPaid = { debtToMarkPaid = it },
        onDeleteDebt = { debtToDelete = it },
    )

    // ── Diálogos y BottomSheets (usan viewModel directamente) ─────────────────
    if (showAdd) {
        AddDebtBottomSheet(onDismiss = { showAdd = false }, viewModel = viewModel)
    }
    debtToEdit?.let {
        AddDebtBottomSheet(
            editingDebt = it,
            onDismiss = { debtToEdit = null },
            viewModel = viewModel
        )
    }
    debtToDelete?.let {
        AlertDialog(
            onDismissRequest = { debtToDelete = null }, containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.common_delete) + " " + stringResource(Res.string.debt_title).lowercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    "¿Eliminar deuda con ${it.personName}?",
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDebt(it.id); debtToDelete = null
                }) { Text("Eliminar", color = MaterialTheme.appColors.expense) }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) {
                    Text(
                        "Cancelar",
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    debtToMarkPaid?.let {
        AlertDialog(
            onDismissRequest = { debtToMarkPaid = null }, containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.debt_mark_paid),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    "¿Marcar como pagado a ${it.personName}?",
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.markAsPaid(it.id); debtToMarkPaid = null
                }) { Text("Sí, pagado", color = MaterialTheme.appColors.income) }
            },
            dismissButton = {
                TextButton(onClick = { debtToMarkPaid = null }) {
                    Text(
                        "Cancelar",
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CONTENT: Stateless. Solo recibe datos planos y callbacks. Puede tener @Preview.
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun DebtListContent(
    uiState: DebtUiState,
    balancesHidden: Boolean,
    onAddClick: () -> Unit,
    onEditDebt: (Debt) -> Unit,
    onMarkPaid: (Debt) -> Unit,
    onDeleteDebt: (Debt) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.background)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.appColors.primary
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    TopBarApp(
                        title = stringResource(Res.string.debt_title),
                        navigateBack = {
                            // TODO: manejar navegación desde el wrapper
                        }
                    )
                    SpacerVerticalApp(8.dp)
                }

                item {
                    HeaderDebtListScreen(
                        totalTheyOwe = uiState.totalTheyOwe,
                        totalIOwe = uiState.totalIOwe,
                        hidden = balancesHidden
                    )
                }

                if (uiState.debtsTheyOwe.isNotEmpty()) {
                    item {
                        DebtSectionHeader(
                            title = stringResource(Res.string.debt_they_owe) + " ↑",
                            total = uiState.totalTheyOwe,
                            color = MaterialTheme.appColors.income,
                            hidden = balancesHidden
                        )
                    }
                    items(uiState.debtsTheyOwe, key = { it.id }) { debt ->
                        SwipeRowApp(
                            titleSwipe = stringResource(Res.string.common_delete),
                            colorSwipe = MaterialTheme.appColors.expense,
                            onDelete = { onDeleteDebt(debt) },
                            content = {
                                DebtCard(
                                    debt = debt,
                                    hidden = balancesHidden,
                                    onMarkPaid = { onMarkPaid(debt) },
                                    onEdit = { onEditDebt(debt) }
                                )
                            }
                        )
                    }
                }

                if (uiState.debtsIOwe.isNotEmpty()) {
                    item {
                        DebtSectionHeader(
                            title = stringResource(Res.string.debt_i_owe) + " ↓",
                            total = uiState.totalIOwe,
                            color = MaterialTheme.appColors.expense,
                            hidden = balancesHidden
                        )
                    }
                    items(uiState.debtsIOwe, key = { it.id }) { debt ->
                        SwipeRowApp(
                            titleSwipe = stringResource(Res.string.common_delete),
                            colorSwipe = MaterialTheme.appColors.expense,
                            onDelete = { onDeleteDebt(debt) },
                            content = {
                                DebtCard(
                                    debt = debt,
                                    hidden = balancesHidden,
                                    onMarkPaid = { onMarkPaid(debt) },
                                    onEdit = { onEditDebt(debt) })
                            }
                        )
                    }
                }

                if (uiState.debtsTheyOwe.isEmpty() && uiState.debtsIOwe.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Outlined.Handshake,
                            title = stringResource(Res.string.debt_no_debts),
                            subtitle = "Pulsa \"Nueva deuda\" para registrar\nuna deuda pendiente"
                        )
                    }
                }

                item {
                    SpacerVerticalApp(8.dp)
                    LargeButtonApp(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.debt_add_title),
                        title = stringResource(Res.string.debt_add_title),
                        onClick = onAddClick
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════════════════════════════════════════════

@Preview
@Composable
fun DebtListContentPreview() {
    val fakeDebts = listOf(
        Debt(
            id = "debt_1",
            accountId = "acc_1",
            personName = "Juan Pérez",
            amount = 150.0,
            direction = DebtDirection.THEY_OWE,
            date = 1704067200000,
            isPaid = false,
            notes = "Préstamo personal",
            createdAt = 1704067200000
        ),
        Debt(
            id = "debt_2",
            accountId = "acc_1",
            personName = "María García",
            amount = 75.0,
            direction = DebtDirection.I_OWE,
            date = 1706745600000,
            isPaid = false,
            notes = null,
            createdAt = 1706745600000
        )
    )

    N3toTheme {
        DebtListContent(
            uiState = DebtUiState(
                debtsTheyOwe = listOf(fakeDebts[0]),
                debtsIOwe = listOf(fakeDebts[1]),
                totalTheyOwe = 150.0,
                totalIOwe = 75.0,
                isLoading = false
            ),
            balancesHidden = false,
            onAddClick = {},
            onEditDebt = {},
            onMarkPaid = {},
            onDeleteDebt = {}
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Subcomponentes (sin cambios, solo se movieron de private a internal)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HeaderDebtListScreen(totalTheyOwe: Double, totalIOwe: Double, hidden: Boolean) {
    Surface(color = MaterialTheme.appColors.background, shadowElevation = 0.dp) {
        Card(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(all = 12.dp)
            ) {
                DebtSummaryCell(
                    label = "Me deben",
                    amount = totalTheyOwe,
                    color = MaterialTheme.appColors.income,
                    hidden = hidden,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.width(1.dp).height(44.dp)
                        .background(MaterialTheme.appColors.border)
                        .align(Alignment.CenterVertically)
                )
                DebtSummaryCell(
                    label = "Debo yo",
                    amount = totalIOwe,
                    color = MaterialTheme.appColors.expense,
                    hidden = hidden,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DebtSummaryCell(
    label: String,
    color: Color,
    amount: Double,
    hidden: Boolean,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        N3toLabel(
            text = label,
            color = MaterialTheme.appColors.textSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            "${maskAmount(formatAmount(amount), hidden)} €",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
private fun DebtSectionHeader(title: String, total: Double, color: Color, hidden: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        N3toLabel(text = title)
        Text(
            "Total: ${maskAmount(formatAmount(total), hidden)} €",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(end = 4.dp)
        )
    }
}

@Composable
private fun DebtCard(debt: Debt, hidden: Boolean, onMarkPaid: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            InitialsAvatar(
                text = debt.personName.firstOrNull()?.uppercase() ?: "?",
                bgColor = if (debt.direction == DebtDirection.THEY_OWE) MaterialTheme.appColors.income else MaterialTheme.appColors.expense,
                size = 38.dp,
                textSize = 15
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    debt.personName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${debt.notes ?: "Sin nota"} · ${formatDate(debt.date)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${maskAmount(formatAmount(debt.amount), hidden)} €",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (debt.direction == DebtDirection.THEY_OWE) MaterialTheme.appColors.income else MaterialTheme.appColors.expense
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onMarkPaid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.appColors.income.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.appColors.income
                        ),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Pagado", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.appColors.surface4.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.appColors.textSecondary
                        ),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Editar", fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// EmptyState reemplazado por EmptyStateView de ui.common.component
