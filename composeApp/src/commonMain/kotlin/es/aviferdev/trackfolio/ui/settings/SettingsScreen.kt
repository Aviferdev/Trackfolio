package es.aviferdev.trackfolio.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.IssuerType
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.security.AppLockManager
import es.aviferdev.trackfolio.security.BiometricAuthenticator
import es.aviferdev.trackfolio.security.BiometricResult
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.account.AddEditAccountBottomSheet
import es.aviferdev.trackfolio.ui.home.SetInitialBalanceBottomSheet
import es.aviferdev.trackfolio.ui.settings.backup.BackupAction
import es.aviferdev.trackfolio.ui.settings.backup.BackupPasswordSheet
import es.aviferdev.trackfolio.ui.settings.backup.BackupViewModel
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateToExpenseSettings: () -> Unit = {},
    onNavigateToIncomeSettings: () -> Unit = {},
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Surface(color = SurfaceWhite, shadowElevation = 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    "Ajustes",
                    fontSize      = 18.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = TextPrimary,
                    letterSpacing = (-0.3).sp
                )
            }
        }
        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)

        LazyColumn(
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── MIS CUENTAS ───────────────────────────────────────────────────
            item { SectionHeader("Mis cuentas", actionLabel = "Añadir", onAction = { accountViewModel.openAddSheet() }) }

            if (accountState.accounts.isEmpty()) {
                item { EmptyAccountsCard(onAdd = { accountViewModel.openAddSheet() }) }
            } else {
                items(accountState.accounts, key = { it.id }) { account ->
                    SettingsAccountCard(
                        account    = account,
                        isSelected = account.id == selectedId,
                        onSelect   = { accountViewModel.selectAccount(account.id) },
                        onEdit     = { accountViewModel.openEditSheet(account) },
                        onDelete   = { accountViewModel.requestDelete(account) }
                    )
                }
            }

            // ── CATEGORÍAS ────────────────────────────────────────────────────
            item { Spacer(Modifier.height(2.dp)) }
            item { SectionHeader("Categorías") }
            item {
                SettingsGroupCard {
                    NavigableRow(icon = "📉", label = "Gastos",    onClick = onNavigateToExpenseSettings)
                    RowDivider()
                    NavigableRow(icon = "📈", label = "Ingresos",  onClick = onNavigateToIncomeSettings)
                }
            }

            // ── PREFERENCIAS ──────────────────────────────────────────────────
            item { Spacer(Modifier.height(2.dp)) }
            item { SectionHeader("Preferencias") }
            item {
                SettingsGroupCard {
                    InfoRow(icon = "🌍", label = "Idioma",              value = "Español")
                    RowDivider()
                    InfoRow(icon = "💱", label = "Moneda por defecto",  value = "EUR")
                }
            }

            // ── RECONCILIACIÓN ────────────────────────────────────────────────
            item { Spacer(Modifier.height(2.dp)) }
            item { SectionHeader("Reconciliación") }
            item {
                val intervalUseCase = koinInject<es.aviferdev.trackfolio.domain.usecase.reconciliation.GetReconciliationReminderIntervalUseCase>()
                var interval by remember { mutableIntStateOf(intervalUseCase.get()) }

                SettingsGroupCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            "Recordatorio de reajuste",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Te recordaré verificar el saldo de tus cuentas de efectivo.",
                            fontSize = 12.sp,
                            color    = TextTertiary
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0 to "Off", 7 to "7d", 15 to "15d", 30 to "30d").forEach { (days, label) ->
                                val selected = interval == days
                                OutlinedButton(
                                    onClick  = { interval = days; intervalUseCase.set(days) },
                                    shape    = RoundedCornerShape(9.dp),
                                    colors   = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) PrimaryDark else Color.Transparent,
                                        contentColor   = if (selected) Color.White else TextSecondary
                                    ),
                                    border   = BorderStroke(
                                        1.dp,
                                        if (selected) PrimaryDark else BorderGray
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            // ── SEGURIDAD ─────────────────────────────────────────────────────
            item { Spacer(Modifier.height(2.dp)) }
            item { SectionHeader("Seguridad") }
            item {
                SettingsGroupCard {
                    BiometricRow(
                        enabled     = biometricEnabled,
                        isAvailable = authenticator.isAvailable(),
                        error       = biometricError,
                        onToggle    = { shouldEnable ->
                            biometricError = null
                            if (shouldEnable) {
                                authenticator.authenticate("Activar bloqueo biométrico", "Confirma tu identidad") { result ->
                                    when (result) {
                                        is BiometricResult.Success -> { lockManager.enableBiometric(); biometricEnabled = true }
                                        is BiometricResult.NotAvailable -> biometricError = "Biometría no disponible"
                                        is BiometricResult.Error        -> biometricError = result.message
                                        else                            -> Unit
                                    }
                                }
                            } else { lockManager.disableBiometric(); biometricEnabled = false }
                        }
                    )
                    RowDivider()
                    ActionRow(icon = "☁️",  label = "Exportar backup",  onClick = { backupViewModel.openExport() })
                    RowDivider()
                    ActionRow(icon = "📥", label = "Importar backup",  onClick = { backupViewModel.openImport() })
                }
            }

            // ── ACERCA DE ─────────────────────────────────────────────────────
            item { Spacer(Modifier.height(2.dp)) }
            item { SectionHeader("Acerca de") }
            item {
                SettingsGroupCard {
                    InfoRow(icon = "📱", label = "Versión",              value = "1.0.0")
                    RowDivider()
                    InfoRow(icon = "⚖️",  label = "Privacidad y términos", value = "")
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // ── Sheets & dialogs ─────────────────────────────────────────────────────
    if (accountState.showAddSheet) {
        AddEditAccountBottomSheet(
            account   = null,
            onSave    = { name, currency, type -> accountViewModel.addAccount(name, currency, type) },
            onDismiss = { accountViewModel.closeAddSheet() }
        )
    }
    accountState.pendingInitialBalanceAccount?.let { pending ->
        SetInitialBalanceBottomSheet(
            accountName = pending.name,
            currency    = pending.currency,
            onConfirm   = { amount -> accountViewModel.confirmInitialBalance(amount) }
        )
    }
    if (accountState.showEditSheet && accountState.editingAccount != null) {
        AddEditAccountBottomSheet(
            account   = accountState.editingAccount,
            onSave    = { name, currency, type ->
                accountViewModel.editAccount(accountState.editingAccount!!, name, currency, type)
            },
            onDismiss = { accountViewModel.closeEditSheet() }
        )
    }
    if (accountState.showDeleteConfirm && accountState.accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { accountViewModel.cancelDelete() },
            containerColor   = SurfaceWhite,
            icon  = { Text("⚠️", fontSize = 26.sp) },
            title = { Text("Eliminar cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text  = {
                Text(
                    "Se eliminará «${accountState.accountToDelete!!.name}» y todos sus movimientos. Esta acción no se puede deshacer.",
                    fontSize = 13.sp,
                    color    = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { accountViewModel.confirmDelete() }) {
                    Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountViewModel.cancelDelete() }) {
                    Text("Cancelar", color = PrimaryDark)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    if (backupState.action != BackupAction.NONE) {
        BackupPasswordSheet(
            state                   = backupState,
            onPasswordChange        = backupViewModel::onPasswordChange,
            onConfirmPasswordChange = backupViewModel::onConfirmPasswordChange,
            onConfirm               = {
                if (backupState.action == BackupAction.EXPORT) backupViewModel.confirmExport()
                else backupViewModel.confirmImport()
            },
            onDismiss = { backupViewModel.dismiss() }
        )
    }
}

// ─── Account card ─────────────────────────────────────────────────────────────
@Composable
private fun SettingsAccountCard(
    account: Account,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryDark else BorderGray,
        label       = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryAlpha else SurfaceWhite,
        label       = "bg"
    )

    Card(
        onClick   = onSelect,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(13.dp),
        border    = BorderStroke(if (isSelected) 1.dp else 0.5.dp, borderColor),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) PrimaryDark else SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    account.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize   = 17.sp,
                    color      = if (isSelected) Color.White else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        account.name,
                        fontSize   = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color      = TextPrimary
                    )
                    if (account.needsInitialBalance) {
                        Spacer(Modifier.width(5.dp))
                        Text("⚠️", fontSize = 11.sp)
                    }
                }
                Text(
                    if (account.needsInitialBalance) "Saldo inicial pendiente" else account.currency,
                    fontSize = 11.sp,
                    color    = if (account.needsInitialBalance) ExpenseRed else TextTertiary
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryDark)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Activa", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(6.dp))
            }
            IconButton(
                onClick  = onEdit,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(15.dp), tint = TextSecondary)
            }
            IconButton(
                onClick  = onDelete,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(15.dp), tint = ExpenseRed)
            }
        }
    }
}

// ─── Reusable row components ──────────────────────────────────────────────────
@Composable
private fun NavigableRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 17.sp, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Text("›", fontSize = 18.sp, color = TextTertiary)
    }
}

@Composable
private fun InfoRow(icon: String, label: String, value: String) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 17.sp, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) Text(value, fontSize = 13.sp, color = TextTertiary)
        else Text("›", fontSize = 18.sp, color = TextTertiary)
    }
}

@Composable
private fun ActionRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 17.sp, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, color = PrimaryDark, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Text("›", fontSize = 18.sp, color = PrimaryDark)
    }
}

@Composable
private fun BiometricRow(
    enabled: Boolean,
    isAvailable: Boolean,
    error: String?,
    onToggle: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔒", fontSize = 17.sp, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Bloqueo con biometría",
                    fontSize = 14.sp,
                    color    = if (isAvailable) TextPrimary else TextTertiary
                )
                if (!isAvailable) {
                    Text("No disponible en este dispositivo", fontSize = 11.sp, color = TextTertiary)
                }
            }
            Switch(
                checked          = enabled,
                onCheckedChange  = { if (isAvailable) onToggle(it) },
                enabled          = isAvailable,
                colors           = SwitchDefaults.colors(
                    checkedThumbColor   = Color.White,
                    checkedTrackColor   = PrimaryDark,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = SurfaceElevated
                )
            )
        }
        error?.let {
            Text(
                it,
                fontSize = 11.sp,
                color    = ExpenseRed,
                modifier = Modifier.padding(start = 54.dp, end = 16.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = 54.dp),
        color     = BorderGray,
        thickness = 0.5.dp
    )
}

@Composable
private fun EmptyAccountsCard(onAdd: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(13.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border    = BorderStroke(0.5.dp, BorderGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏦", fontSize = 30.sp)
            Spacer(Modifier.height(8.dp))
            Text("Sin cuentas todavía", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Crea tu primera cuenta para empezar", fontSize = 12.sp, color = TextTertiary)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onAdd,
                shape   = RoundedCornerShape(10.dp),
                border  = BorderStroke(1.dp, PrimaryDark)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(15.dp), tint = PrimaryDark)
                Spacer(Modifier.width(5.dp))
                Text("Añadir cuenta", color = PrimaryDark, fontSize = 13.sp)
            }
        }
    }
}

// ─── Public helpers (used by sub-screens) ────────────────────────────────────
@Composable
internal fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text          = title.uppercase(),
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            color         = TextTertiary,
            letterSpacing = 0.7.sp
        )
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick        = onAction,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
            ) {
                Text(actionLabel, fontSize = 12.sp, color = PrimaryDark, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
internal fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(13.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border    = BorderStroke(0.5.dp, BorderGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(content = content)
    }
}

// ─── Sheets (unchanged logic, updated colors) ─────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddCategorySheet(
    type: TransactionType,
    onSave: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name      by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    val typeLabel = if (type == TransactionType.INCOME) "ingreso" else "gasto"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text("Nueva categoría de $typeLabel", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(22.dp))
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text("Nombre de la categoría") },
                placeholder   = { Text("Ej. Mascotas, Gimnasio…") },
                isError       = nameError,
                supportingText = if (nameError) {{ Text("El nombre es obligatorio") }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(26.dp))
            Button(
                onClick  = { if (name.isBlank()) { nameError = true; return@Button }; onSave(name.trim()) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) { Text("Crear categoría", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", fontSize = 14.sp, color = TextTertiary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditCategorySheet(
    currentName: String,
    type: TransactionType,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name      by remember { mutableStateOf(currentName) }
    var nameError by remember { mutableStateOf(false) }
    val typeLabel = if (type == TransactionType.INCOME) "ingreso" else "gasto"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text("Editar categoría de $typeLabel", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(22.dp))
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text("Nombre de la categoría") },
                isError       = nameError,
                supportingText = if (nameError) {{ Text("El nombre es obligatorio") }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(26.dp))
            Button(
                onClick  = { if (name.isBlank()) { nameError = true; return@Button }; onSave(name.trim()) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) { Text("Guardar cambios", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", fontSize = 14.sp, color = TextTertiary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditIssuerSheet(
    initial: es.aviferdev.trackfolio.domain.model.Issuer?,
    type: IssuerType,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = initial != null
    var name      by remember { mutableStateOf(initial?.name ?: "") }
    var icon      by remember { mutableStateOf(initial?.icon ?: defaultIconForType(type)) }
    var nameError by remember { mutableStateOf(false) }
    val icons     = listOf("🏢","🏦","📜","📈","🎁","💰","💵","🪙","🔒","💼","⚙️","🌟","🚀","⚖️").distinct()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderGray2)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                if (isEditing) "Editar ${type.label.lowercase()}"
                else "Nuevo/a ${type.label.lowercase()}",
                fontSize   = 17.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(18.dp))
            Text("Icono", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icons.forEach { ic ->
                    val sel = ic == icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) PrimaryAlpha else SurfaceElevated)
                            .border(
                                if (sel) 1.dp else 0.5.dp,
                                if (sel) PrimaryDark else BorderGray,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { icon = ic },
                        contentAlignment = Alignment.Center
                    ) { Text(ic, fontSize = 18.sp) }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text("Nombre") },
                placeholder   = { Text("Ej. ${type.label}") },
                isError       = nameError,
                supportingText = if (nameError) {{ Text("El nombre es obligatorio") }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(26.dp))
            Button(
                onClick  = { if (name.isBlank()) { nameError = true; return@Button }; onSave(name.trim(), icon) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Text(if (isEditing) "Guardar cambios" else "Crear", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", fontSize = 14.sp, color = TextTertiary)
            }
        }
    }
}

internal fun defaultIconForType(type: IssuerType): String = when (type) {
    IssuerType.EMPLOYER           -> "🏢"
    IssuerType.BANK               -> "🏦"
    IssuerType.BOND_ISSUER        -> "📜"
    IssuerType.DIVIDEND_SOURCE    -> "📈"
    IssuerType.PROMOTION_PLATFORM -> "🎁"
    IssuerType.EXEMPT_SOURCE      -> "📋"
}
