package es.aviferdev.trackfolio.ui.account

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.AccountType
import es.aviferdev.trackfolio.ui.common.navigation.TopBarApp
import es.aviferdev.trackfolio.ui.home.SetInitialBalanceBottomSheet
import es.aviferdev.trackfolio.ui.theme.BackgroundGray
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.LocalBalanceHidden
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import es.aviferdev.trackfolio.ui.theme.maskAmount
import kotlinx.datetime.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// WRAPPER
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AccountListScreen(
    viewModel: AccountViewModel = koinViewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val selectedId by viewModel.selectedAccountId.collectAsState()
    val balancesHidden = LocalBalanceHidden.current

    AccountListContent(
        uiState         = uiState,
        selectedId      = selectedId,
        balancesHidden  = balancesHidden,
        onAddClick      = { viewModel.openAddSheet() },
        onSelectAccount = { viewModel.selectAccount(it) },
        onEditAccount   = { viewModel.openEditSheet(it) },
        onDeleteAccount = { viewModel.requestDelete(it) }
    )

    // ── Diálogos / Sheets (usan viewModel) ─────────────────────────────────────
    if (uiState.showAddSheet) {
        AddEditAccountBottomSheet(
            account   = null,
            onSave    = { name, currency, type -> viewModel.addAccount(name, currency, type) },
            onDismiss = { viewModel.closeAddSheet() }
        )
    }
    uiState.pendingInitialBalanceAccount?.let { pending ->
        SetInitialBalanceBottomSheet(
            accountName = pending.name,
            currency    = pending.currency,
            onConfirm   = { amount -> viewModel.confirmInitialBalance(amount) },
        )
    }
    if (uiState.showEditSheet && uiState.editingAccount != null) {
        AddEditAccountBottomSheet(
            account   = uiState.editingAccount,
            onSave    = { name, currency, type ->
                viewModel.editAccount(uiState.editingAccount!!, name, currency, type)
            },
            onDismiss = { viewModel.closeEditSheet() }
        )
    }
    if (uiState.showDeleteConfirm && uiState.accountToDelete != null) {
        DeleteAccountDialog(
            account   = uiState.accountToDelete!!,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }
    uiState.error?.let {
        LaunchedEffect(it) { viewModel.clearError() }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AccountListContent(
    uiState: AccountUiState,
    selectedId: String?,
    balancesHidden: Boolean,
    onAddClick: () -> Unit,
    onSelectAccount: (String) -> Unit,
    onEditAccount: (Account) -> Unit,
    onDeleteAccount: (Account) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(BackgroundGray)) {
        TopBarApp(
            title = "Mis cuentas",
            actions = {
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir cuenta", tint = TextPrimary)
                }
            }
        )

        if (uiState.accounts.isEmpty()) {
            EmptyAccountsState(
                modifier = Modifier.fillMaxSize(),
                onAdd    = onAddClick
            )
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.accounts, key = { it.id }) { account ->
                    AccountCard(
                        account        = account,
                        isSelected     = account.id == selectedId,
                        balancesHidden = balancesHidden,
                        onSelect       = { onSelectAccount(account.id) },
                        onEdit         = { onEditAccount(account) },
                        onDelete       = { onDeleteAccount(account) }
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
fun AccountListContentPreview() {
    val now = Clock.System.now().toEpochMilliseconds()
    val fakeAccounts = listOf(
        Account(id = "1", name = "Cuenta Principal", currency = "EUR",
            initialBalance = 5000.0, computedBalance = 5200.0, createdAt = now, accountType = AccountType.GENERAL),
        Account(id = "2", name = "Efectivo", currency = "EUR",
            initialBalance = 0.0, computedBalance = 0.0, createdAt = now, accountType = AccountType.CASH),
        Account(id = "3", name = "USD Savings", currency = "USD",
            initialBalance = 1000.0, computedBalance = 1050.0, createdAt = now, accountType = AccountType.GENERAL)
    )

    TrackfolioTheme {
        AccountListContent(
            uiState = AccountUiState(
                accounts = fakeAccounts,
                isLoading = false
            ),
            selectedId = "1",
            balancesHidden = false,
            onAddClick = {},
            onSelectAccount = {},
            onEditAccount = {},
            onDeleteAccount = {}
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Subcomponentes (sin cambios)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AccountCard(
    account: Account,
    isSelected: Boolean,
    balancesHidden: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryDark.copy(alpha = 0.6f) else BorderGray,
        label       = "borderColor"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFE8EDF5) else SurfaceWhite,
        label       = "containerColor"
    )

    Card(
        onClick   = onSelect,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        border    = BorderStroke(if (isSelected) 2.dp else 0.5.dp, borderColor),
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier        = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PrimaryDark else BackgroundGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = account.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (isSelected) Color.White else TextSecondary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text       = account.name,
                                fontSize   = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color      = TextPrimary
                            )
                            if (account.needsInitialBalance) {
                                Spacer(Modifier.width(6.dp))
                                Text("⚠️", fontSize = 12.sp)
                            }
                        }
                        Text(
                            text  = if (account.needsInitialBalance) "Saldo inicial pendiente" else account.currency,
                            fontSize = 12.sp,
                            color = if (account.needsInitialBalance) ExpenseRed else TextSecondary
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp), tint = TextSecondary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(18.dp), tint = ExpenseRed)
                    }
                }
            }

            if (!account.needsInitialBalance) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Bottom
                ) {
                    Column {
                        Text("Saldo actual", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text       = "${maskAmount(formatAmount(account.computedBalance), balancesHidden)} ${account.currency}",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (account.computedBalance >= 0) PrimaryDark else ExpenseRed
                        )
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(PrimaryDark)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Activa", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAccountsState(modifier: Modifier, onAdd: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏦", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text("Sin cuentas todavía", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                "Crea tu primera cuenta desde\nAjustes para empezar",
                fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAdd, shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Añadir cuenta")
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(account: Account, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceWhite,
        icon             = { Text("⚠️", fontSize = 32.sp) },
        title            = { Text("Eliminar cuenta", fontWeight = FontWeight.SemiBold) },
        text             = {
            Text(
                "Se eliminará «${account.name}» junto con todos sus movimientos y deudas. Esta acción no se puede deshacer.",
                fontSize = 14.sp, color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.Medium) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium) }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

private fun formatAmount(amount: Double): String {
    val abs     = if (amount < 0) -amount else amount
    val rounded = (abs * 100).toLong()
    val euros   = rounded / 100
    val cents   = rounded % 100
    val eurosStr = buildString {
        euros.toString().reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append('.')
            append(c)
        }
    }.reversed()
    return "$eurosStr,${cents.toString().padStart(2, '0')}"
}
