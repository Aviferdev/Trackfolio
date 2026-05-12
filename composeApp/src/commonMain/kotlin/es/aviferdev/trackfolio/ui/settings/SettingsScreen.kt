package es.aviferdev.trackfolio.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.AccountType
import es.aviferdev.trackfolio.domain.usecase.reconciliation.GetReconciliationReminderIntervalUseCase
import es.aviferdev.trackfolio.security.AppLockManager
import es.aviferdev.trackfolio.security.BiometricAuthenticator
import es.aviferdev.trackfolio.security.BiometricResult
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.account.AddEditAccountBottomSheet
import es.aviferdev.trackfolio.ui.common.*
import es.aviferdev.trackfolio.ui.common.navigation.TopBarApp
import es.aviferdev.trackfolio.ui.settings.backup.BackupViewModel
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// ─── WRAPPER ────────────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(
    onNavigateToExpenseSettings: () -> Unit = {},
    onNavigateToIncomeSettings: () -> Unit = {},
    onNavigateToFiscalReport: () -> Unit = {},
    accountViewModel: AccountViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel()
) {
    val accountState by accountViewModel.uiState.collectAsState()
    val selectedId   by accountViewModel.selectedAccountId.collectAsState()
    val backupState  by backupViewModel.state.collectAsState()
    val authenticator: BiometricAuthenticator = koinInject()
    val lockManager: AppLockManager           = koinInject()

    var biometricEnabled by remember { mutableStateOf(lockManager.biometricEnabled) }
    var biometricError   by remember { mutableStateOf<String?>(null) }

    val reconciliationIntervalUseCase = koinInject<GetReconciliationReminderIntervalUseCase>()
    var reconciliationInterval by remember { mutableStateOf(reconciliationIntervalUseCase.get()) }

    SettingsContent(
        reconciliationInterval = reconciliationInterval,
        onReconciliationIntervalChange = { days ->
            reconciliationInterval = days
            reconciliationIntervalUseCase.set(days)
        },
        accounts = accountState.accounts,
        selectedId = selectedId,
        biometricEnabled = biometricEnabled,
        onAddAccount = { accountViewModel.openAddSheet() },
        onSelectAccount = { id -> accountViewModel.selectAccount(id) },
        onEditAccount = { account -> accountViewModel.openEditSheet(account) },
        onDeleteAccount = { account -> accountViewModel.requestDelete(account) },
        onToggleBiometric = { enabled ->
            if (enabled) {
                authenticator.authenticate("Activar biometría", "Confirma tu identidad") { result ->
                    when (result) {
                        is BiometricResult.Success -> { lockManager.enableBiometric(); biometricEnabled = true }
                        is BiometricResult.Error -> biometricError = result.message
                        else -> {}
                    }
                }
            } else {
                lockManager.disableBiometric()
                biometricEnabled = false
            }
        },
        onNavigateToExpenseSettings = onNavigateToExpenseSettings,
        onNavigateToIncomeSettings = onNavigateToIncomeSettings,
        onNavigateToFiscalReport = onNavigateToFiscalReport
    )

    // ── Sheets ───────────────────────────────────────────────────────────────
    if (accountState.showAddSheet) {
        AddEditAccountBottomSheet(account = null, onSave = { name, currency, type -> accountViewModel.addAccount(name, currency, type) }, onDismiss = { accountViewModel.closeAddSheet() })
    }
    if (accountState.showEditSheet && accountState.editingAccount != null) {
        AddEditAccountBottomSheet(account = accountState.editingAccount, onSave = { name, currency, type -> accountViewModel.editAccount(accountState.editingAccount!!, name, currency, type) }, onDismiss = { accountViewModel.closeEditSheet() })
    }
    if (accountState.showDeleteConfirm && accountState.accountToDelete != null) {
        AlertDialog(onDismissRequest = { accountViewModel.cancelDelete() }, containerColor = SurfaceWhite,
            title = { Text("Eliminar cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text  = { Text("¿Eliminar \"${accountState.accountToDelete!!.name}\"? Esto también eliminará todos sus movimientos.", fontSize = 13.sp, color = TextSecondary) },
            confirmButton = { TextButton(onClick = { accountViewModel.confirmDelete() }) { Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.SemiBold) } },
            dismissButton = { TextButton(onClick = { accountViewModel.cancelDelete() }) { Text("Cancelar", color = PrimaryDark) } },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ─── CONTENT ────────────────────────────────────────────────────────────────────
@Composable
fun SettingsContent(
    accounts: List<Account>,
    selectedId: String?,
    biometricEnabled: Boolean,
    onAddAccount: () -> Unit,
    onSelectAccount: (String) -> Unit,
    onEditAccount: (Account) -> Unit,
    onDeleteAccount: (Account) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onNavigateToExpenseSettings: () -> Unit,
    onNavigateToIncomeSettings: () -> Unit,
    onNavigateToFiscalReport: () -> Unit,
    reconciliationInterval: Int = 30,
    onReconciliationIntervalChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    Column(modifier = modifier.fillMaxSize().background(BackgroundGray)) {
        TopBarApp(title = "Ajustes")

        AnimatedVisibility(visible = contentVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })) {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    SettingsSectionHeader(label = "Cuentas", actionLabel = "Añadir", onAction = onAddAccount)
                }

                if (accounts.isEmpty()) {
                    item { EmptyAccountsCard(onAdd = onAddAccount) }
                } else {
                    items(accounts, key = { it.id }) { account ->
                        SettingsAccountCard(account = account, isSelected = account.id == selectedId, onSelect = { onSelectAccount(account.id) }, onEdit = { onEditAccount(account) }, onDelete = { onDeleteAccount(account) })
                    }
                }

                item {
                    SettingsGroupCard {
                        SettingsNavigableRow(icon = "📉", label = "Categorías de gastos", onClick = onNavigateToExpenseSettings)
                        SettingsRowDivider()
                        SettingsNavigableRow(icon = "📈", label = "Tipos de ingresos",   onClick = onNavigateToIncomeSettings)
                    }
                }

                item {
                    SettingsSectionHeader(label = "Seguridad")
                    SettingsGroupCard {
                        SettingsBiometricRow(enabled = biometricEnabled, onToggle = onToggleBiometric)
                    }
                }

                item {
                    SettingsSectionHeader(label = "Recordatorios")
                    SettingsGroupCard {
                        SettingsReconciliationIntervalRow(
                            interval    = reconciliationInterval,
                            onIntervalChange = onReconciliationIntervalChange
                        )
                    }
                }

                item {
                    SettingsSectionHeader(label = "Datos")
                    SettingsGroupCard {
                        SettingsNavigableRow(icon = "🧾", label = "Informe fiscal IRPF", onClick = onNavigateToFiscalReport)
                        SettingsRowDivider()
                        SettingsNavigableRow(icon = "💾", label = "Copia de seguridad",   onClick = { })
                    }
                }

                item { Spacer(Modifier.height(60.dp)) }
            }
        }
    }
}

// ─── PREVIEW ────────────────────────────────────────────────────────────────────
@Preview
@Composable
fun SettingsContentPreview() {
    TrackfolioTheme {
        SettingsContent(
            accounts = listOf(
                Account(id = "1", name = "Cuenta principal", currency = "€", initialBalance = 1000.0, computedBalance = 1500.0, createdAt = 0L, accountType = AccountType.GENERAL),
                Account(id = "2", name = "Efectivo", currency = "€", initialBalance = 0.0, computedBalance = 500.0, createdAt = 0L, accountType = AccountType.CASH)
            ),
            selectedId = "1",
            biometricEnabled = false,
            onAddAccount = {},
            onSelectAccount = {},
            onEditAccount = {},
            onDeleteAccount = {},
            onToggleBiometric = {},
            onNavigateToExpenseSettings = {},
            onNavigateToIncomeSettings = {},
            onNavigateToFiscalReport = {}
        )
    }
}

// ─── Settings section header ──────────────────────────────────────────────────
@Composable
private fun SettingsSectionHeader(label: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        TrackfolioLabel(text = label)
        if (actionLabel != null && onAction != null) {
            Text(actionLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PrimaryDark, modifier = Modifier.clickable { onAction() })
        }
    }
}

// ─── Settings group card ─────────────────────────────────────────────────────
@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(content = content)
    }
}

// ─── Row divider ─────────────────────────────────────────────────────────────
@Composable
private fun SettingsRowDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = BorderGray, thickness = 0.5.dp)
}

// ─── Navigable row ────────────────────────────────────────────────────────────
@Composable
private fun SettingsNavigableRow(icon: String, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
    }
}

// ─── Biometric toggle row ───────────────────────────────────────────────────
@Composable
private fun SettingsBiometricRow(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("🔐", fontSize = 18.sp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Bloqueo biométrico", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(if (enabled) "Activado" else "Desactivado", fontSize = 11.sp, color = TextTertiary)
        }
        Switch(checked = enabled, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryDark, uncheckedThumbColor = Color.White, uncheckedTrackColor = SurfaceElevated))
    }
}

// ─── Reconciliation interval row ──────────────────────────────────────────────
@Composable
private fun SettingsReconciliationIntervalRow(
    interval: Int,
    onIntervalChange: (Int) -> Unit,
) {
    val options = listOf(
        0  to "Desactivado",
        7  to "7 días",
        15 to "15 días",
        30 to "30 días"
    )
    val label = options.find { it.first == interval }?.second ?: "30 días"

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔄", fontSize = 18.sp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Recordatorio de reconciliación", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(label, fontSize = 11.sp, color = TextTertiary)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (days, text) ->
                val selected = interval == days
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) PrimaryDark.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            if (selected) 1.5.dp else 0.5.dp,
                            if (selected) PrimaryDark else BorderGray,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onIntervalChange(days) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text,
                        fontSize   = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (selected) PrimaryDark else TextSecondary
                    )
                }
            }
        }
    }
}

// ─── Empty accounts card ─────────────────────────────────────────────────────
@Composable
private fun EmptyAccountsCard(onAdd: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onAdd), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryDark, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Añadir cuenta", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PrimaryDark)
        }
    }
}

// ─── Settings account card ──────────────────────────────────────────────────
@Composable
private fun SettingsAccountCard(account: Account, isSelected: Boolean, onSelect: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().then(if (isSelected) Modifier.border(1.dp, PrimaryDark, RoundedCornerShape(12.dp)) else Modifier), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("${account.currency} · ${formatAmount(account.computedBalance)} €", fontSize = 11.sp, color = TextTertiary)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextSecondary, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ExpenseRed, modifier = Modifier.size(16.dp)) }
        }
    }
}
