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
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SettingsScreen(
    onNavigateToFiscalReport: () -> Unit = {},
    onNavigateToExpenseSettings: () -> Unit = {},
    onNavigateToIncomeSettings: () -> Unit = {},
    accountViewModel:  AccountViewModel  = koinViewModel(),
    backupViewModel:   BackupViewModel   = koinViewModel()
) {
    val accountState by accountViewModel.uiState.collectAsState()
    val selectedId   by accountViewModel.selectedAccountId.collectAsState()
    val backupState  by backupViewModel.state.collectAsState()

    val authenticator: BiometricAuthenticator = koinInject()
    val lockManager: AppLockManager           = koinInject()

    var biometricEnabled by remember { mutableStateOf(lockManager.biometricEnabled) }
    var biometricError   by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundGray)
    ) {
        Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text("Ajustes", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
        }

        LazyColumn(
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── MIS CUENTAS ───────────────────────────────────────────────────
            item {
                SectionHeader(
                    title       = "MIS CUENTAS",
                    actionLabel = "Añadir",
                    onAction    = { accountViewModel.openAddSheet() }
                )
            }
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

            // ── CATEGORÍAS (navegación a subpantallas) ────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item { SectionHeader(title = "CATEGORÍAS") }
            item {
                SettingsGroupCard {
                    NavigableSettingsRow(
                        icon    = "\uD83D\uDCC9",
                        label   = "Gastos",
                        onClick = onNavigateToExpenseSettings
                    )
                    HorizontalDivider(
                        color     = BorderGray,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(start = 52.dp)
                    )
                    NavigableSettingsRow(
                        icon    = "\uD83D\uDCC8",
                        label   = "Ingresos",
                        onClick = onNavigateToIncomeSettings
                    )
                }
            }

            // ── PREFERENCIAS ─────────────────────────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item { SectionHeader(title = "PREFERENCIAS") }
            item {
                SettingsGroupCard {
                    SettingsRow(icon = "\uD83C\uDF0D", label = "Idioma", value = "Español")
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    SettingsRow(icon = "\uD83D\uDCB1", label = "Moneda por defecto", value = "EUR")
                }
            }

            // ── SEGURIDAD ─────────────────────────────────────────────────────
            item { SectionHeader(title = "SEGURIDAD") }
            item {
                SettingsGroupCard {
                    BiometricToggleRow(
                        enabled     = biometricEnabled,
                        isAvailable = authenticator.isAvailable(),
                        error       = biometricError,
                        onToggle    = { shouldEnable ->
                            biometricError = null
                            if (shouldEnable) {
                                authenticator.authenticate(
                                    title    = "Activar bloqueo biométrico",
                                    subtitle = "Confirma tu identidad"
                                ) { result ->
                                    when (result) {
                                        is BiometricResult.Success -> {
                                            lockManager.enableBiometric()
                                            biometricEnabled = true
                                        }
                                        is BiometricResult.NotAvailable ->
                                            biometricError = "Biometría no disponible en este dispositivo"
                                        is BiometricResult.Error ->
                                            biometricError = result.message
                                        else -> Unit
                                    }
                                }
                            } else {
                                lockManager.disableBiometric()
                                biometricEnabled = false
                            }
                        }
                    )
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    BackupActionRow(icon = "\u2601\uFE0F",  label = "Exportar backup",  onClick = { backupViewModel.openExport() })
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    BackupActionRow(icon = "\uD83D\uDCE5", label = "Importar backup",  onClick = { backupViewModel.openImport() })
                }
            }

            // ── INFORMES ──────────────────────────────────────────────────────
            item { SectionHeader(title = "INFORMES") }
            item {
                SettingsGroupCard {
                    BackupActionRow(
                        icon    = "\uD83E\uDDFE",
                        label   = "Informe fiscal (PDF)",
                        onClick = onNavigateToFiscalReport
                    )
                }
            }

            // ── ACERCA DE ─────────────────────────────────────────────────────
            item { SectionHeader(title = "ACERCA DE") }
            item {
                SettingsGroupCard {
                    SettingsRow(icon = "\uD83D\uDCF1", label = "Versión", value = "1.0.0")
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    SettingsRow(icon = "\u2696\uFE0F", label = "Privacidad y términos", value = "")
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    // ── Sheets y diálogos ────────────────────────────────────────────────────

    if (accountState.showAddSheet) {
        AddEditAccountBottomSheet(
            account   = null,
            onSave    = { name, currency -> accountViewModel.addAccount(name, currency) },
            onDismiss = { accountViewModel.closeAddSheet() }
        )
    }

    accountState.pendingInitialBalanceAccount?.let { pending ->
        SetInitialBalanceBottomSheet(
            accountName = pending.name,
            currency    = pending.currency,
            onConfirm   = { amount -> accountViewModel.confirmInitialBalance(amount) },
            onDismiss   = {}
        )
    }

    if (accountState.showEditSheet && accountState.editingAccount != null) {
        AddEditAccountBottomSheet(
            account   = accountState.editingAccount,
            onSave    = { name, currency ->
                accountViewModel.editAccount(accountState.editingAccount!!, name, currency)
            },
            onDismiss = { accountViewModel.closeEditSheet() }
        )
    }

    if (accountState.showDeleteConfirm && accountState.accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { accountViewModel.cancelDelete() },
            containerColor   = SurfaceWhite,
            icon             = { Text("\u26A0\uFE0F", fontSize = 28.sp) },
            title = { Text("Eliminar cuenta", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text  = {
                Text(
                    "Se eliminará «${accountState.accountToDelete!!.name}» y todos sus movimientos y deudas. Esta acción no se puede deshacer.",
                    fontSize = 14.sp, color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { accountViewModel.confirmDelete() }) {
                    Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountViewModel.cancelDelete() }) {
                    Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
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

// ── AddCategorySheet ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddCategorySheet(type: TransactionType, onSave: (name: String) -> Unit, onDismiss: () -> Unit) {
    var name      by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    val color     = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
    val typeLabel = if (type == TransactionType.INCOME) "ingreso" else "gasto"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding()
                .padding(horizontal = 24.dp).padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(10.dp))
                Text("Nueva categoría de $typeLabel", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it; nameError = false },
                label = { Text("Nombre de la categoría") },
                placeholder = { Text("Ej. Mascotas, Gimnasio\u2026") },
                isError = nameError,
                supportingText = if (nameError) {{ Text("El nombre es obligatorio") }} else null,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray)
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { if (name.isBlank()) { nameError = true; return@Button }; onSave(name.trim()) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) { Text("Crear categoría", fontSize = 16.sp, fontWeight = FontWeight.Medium) }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

// ── EditCategorySheet ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditCategorySheet(currentName: String, type: TransactionType, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name      by remember { mutableStateOf(currentName) }
    var nameError by remember { mutableStateOf(false) }
    val color     = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
    val typeLabel = if (type == TransactionType.INCOME) "ingreso" else "gasto"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding()
                .padding(horizontal = 24.dp).padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(10.dp))
                Text("Editar categoría de $typeLabel", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it; nameError = false },
                label = { Text("Nombre de la categoría") },
                isError = nameError,
                supportingText = if (nameError) {{ Text("El nombre es obligatorio") }} else null,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray)
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { if (name.isBlank()) { nameError = true; return@Button }; onSave(name.trim()) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) { Text("Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.Medium) }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

// ── AddEditIssuerSheet ──────────────────────────────────────────────────────
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
    val suggestedIcons = listOf(
        "\uD83C\uDFE2", "\uD83C\uDFE6", "\uD83D\uDCDC", "\uD83D\uDCC8", "\uD83C\uDF81",
        "\uD83D\uDCB0", "\uD83D\uDCB5", "\uD83E\uDE99", "\uD83D\uDD12", "\uD83D\uDCBC",
        "\u2699\uFE0F", "\uD83C\uDF1F", "\uD83D\uDE80", "\u2696\uFE0F"
    ).distinct()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        dragHandle = {
            Box(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp).width(40.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(BorderGray))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding()
                .padding(horizontal = 24.dp).padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                if (isEditing) "Editar ${type.label.lowercase()}" else "Nuevo/a ${type.label.lowercase()}",
                fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
            )
            Spacer(Modifier.height(20.dp))
            Text("Icono", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedIcons.forEach { ic ->
                    val isSel = ic == icon
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) SurfaceElevated else SurfaceWhite)
                            .border(if (isSel) 1.5.dp else 0.5.dp, if (isSel) PrimaryDark else BorderGray, RoundedCornerShape(10.dp))
                            .clickable { icon = ic },
                        contentAlignment = Alignment.Center
                    ) { Text(ic, fontSize = 18.sp) }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it; nameError = false },
                label = { Text("Nombre") },
                placeholder = { Text("Ej. ${type.label}") },
                isError = nameError,
                supportingText = if (nameError) {{ Text("El nombre es obligatorio") }} else null,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray)
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { if (name.isBlank()) { nameError = true; return@Button }; onSave(name.trim(), icon) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Text(if (isEditing) "Guardar cambios" else "Crear", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

internal fun defaultIconForType(type: IssuerType): String = when (type) {
    IssuerType.EMPLOYER           -> "\uD83C\uDFE2"
    IssuerType.BANK               -> "\uD83C\uDFE6"
    IssuerType.BOND_ISSUER        -> "\uD83D\uDCDC"
    IssuerType.DIVIDEND_SOURCE    -> "\uD83D\uDCC8"
    IssuerType.PROMOTION_PLATFORM -> "\uD83C\uDF81"
    IssuerType.EXEMPT_SOURCE      -> "\uD83D\uDCCB"
}

// ── SettingsAccountCard ──────────────────────────────────────────────────────
@Composable
private fun SettingsAccountCard(
    account: Account, isSelected: Boolean,
    onSelect: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit
) {
    val borderColor by animateColorAsState(targetValue = if (isSelected) PrimaryDark.copy(alpha = 0.6f) else BorderGray, label = "border")
    val bgColor by animateColorAsState(targetValue = if (isSelected) SurfaceElevated else SurfaceWhite, label = "bg")

    Card(
        onClick = onSelect, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(if (isSelected) PrimaryDark else BackgroundGray),
                contentAlignment = Alignment.Center
            ) {
                Text(account.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?", fontSize = 18.sp, color = if (isSelected) Color.White else TextSecondary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(account.name, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium, color = TextPrimary)
                    if (account.needsInitialBalance) { Spacer(Modifier.width(6.dp)); Text("\u26A0\uFE0F", fontSize = 12.sp) }
                }
                Text(if (account.needsInitialBalance) "Saldo inicial pendiente" else account.currency, fontSize = 12.sp, color = if (account.needsInitialBalance) ExpenseRed else TextSecondary)
            }
            if (isSelected) {
                Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(PrimaryDark).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("Activa", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.width(8.dp))
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = TextSecondary) }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = ExpenseRed) }
        }
    }
}

// ── Componentes auxiliares ───────────────────────────────────────────────────
@Composable
internal fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Text(actionLabel, fontSize = 13.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
internal fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(0.5.dp, BorderGray), elevation = CardDefaults.cardElevation(0.dp)) { Column(content = content) }
}

@Composable
private fun SettingsRow(icon: String, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) Text(value, fontSize = 13.sp, color = TextSecondary)
        else Text("\u203A", fontSize = 18.sp, color = TextSecondary)
    }
}

@Composable
private fun NavigableSettingsRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Text("\u203A", fontSize = 18.sp, color = TextSecondary)
    }
}

@Composable
private fun BackupActionRow(icon: String, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, color = PrimaryDark, modifier = Modifier.weight(1f))
        Text("\u203A", fontSize = 18.sp, color = PrimaryDark)
    }
}

@Composable
private fun BiometricToggleRow(enabled: Boolean, isAvailable: Boolean, error: String?, onToggle: (Boolean) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("\uD83D\uDD12", fontSize = 18.sp, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Bloqueo con biometría", fontSize = 15.sp, color = if (isAvailable) TextPrimary else TextSecondary)
                if (!isAvailable) Text("No disponible en este dispositivo", fontSize = 11.sp, color = TextSecondary)
            }
            Switch(checked = enabled, onCheckedChange = { if (isAvailable) onToggle(it) }, enabled = isAvailable, colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = PrimaryDark, uncheckedThumbColor = SurfaceWhite, uncheckedTrackColor = BorderGray))
        }
        error?.let { Text(it, fontSize = 11.sp, color = ExpenseRed, modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)) }
    }
}

@Composable
private fun EmptyAccountsCard(onAdd: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(0.5.dp, BorderGray), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("\uD83C\uDFE6", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text("Sin cuentas todavía", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Crea tu primera cuenta para empezar", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onAdd, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, PrimaryDark)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = PrimaryDark)
                Spacer(Modifier.width(6.dp))
                Text("Añadir cuenta", color = PrimaryDark, fontSize = 14.sp)
            }
        }
    }
}
