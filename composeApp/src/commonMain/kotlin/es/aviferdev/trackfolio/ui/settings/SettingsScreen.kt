package es.aviferdev.trackfolio.ui.settings

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.account.AddEditAccountBottomSheet
import es.aviferdev.trackfolio.ui.home.SetInitialBalanceBottomSheet
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    accountViewModel: AccountViewModel = koinViewModel()
) {
    val accountState by accountViewModel.uiState.collectAsState()
    val selectedId   by accountViewModel.selectedAccountId.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
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
            // ── Cuentas ──────────────────────────────────────────────────────
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

            // ── Preferencias ─────────────────────────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item { SectionHeader(title = "PREFERENCIAS") }
            item {
                SettingsGroupCard {
                    SettingsRow(icon = "🌍", label = "Idioma", value = "Español")
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    SettingsRow(icon = "💱", label = "Moneda por defecto", value = "EUR")
                }
            }

            // ── Seguridad ─────────────────────────────────────────────────────
            item { SectionHeader(title = "SEGURIDAD") }
            item {
                SettingsGroupCard {
                    SettingsRow(icon = "🔒", label = "Bloqueo con biometría", value = "Próximamente")
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    SettingsRow(icon = "☁️", label = "Copia de seguridad", value = "Próximamente")
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    SettingsRow(icon = "🔔", label = "Recordatorios", value = "Próximamente")
                }
            }

            // ── Acerca de ─────────────────────────────────────────────────────
            item { SectionHeader(title = "ACERCA DE") }
            item {
                SettingsGroupCard {
                    SettingsRow(icon = "📱", label = "Versión", value = "1.0.0")
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    SettingsRow(icon = "⚖️", label = "Privacidad y términos", value = "")
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    // ── Sheets y diálogos ────────────────────────────────────────────────────

    // Crear cuenta
    if (accountState.showAddSheet) {
        AddEditAccountBottomSheet(
            account   = null,
            onSave    = { name, currency ->
                accountViewModel.addAccount(name, currency)
            },
            onDismiss = { accountViewModel.closeAddSheet() }
        )
    }

    // Saldo inicial obligatorio tras crear cuenta
    accountState.pendingInitialBalanceAccount?.let { pendingAccount ->
        SetInitialBalanceBottomSheet(
            accountName = pendingAccount.name,
            currency    = pendingAccount.currency,
            onConfirm   = { amount -> accountViewModel.confirmInitialBalance(amount) },
            onDismiss   = { /* No se puede cancelar — el sheet no tiene botón de omitir */ }
        )
    }

    // Editar cuenta
    if (accountState.showEditSheet && accountState.editingAccount != null) {
        AddEditAccountBottomSheet(
            account   = accountState.editingAccount,
            onSave    = { name, currency ->
                accountViewModel.editAccount(accountState.editingAccount!!, name, currency)
            },
            onDismiss = { accountViewModel.closeEditSheet() }
        )
    }

    // Confirmar borrado
    if (accountState.showDeleteConfirm && accountState.accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { accountViewModel.cancelDelete() },
            containerColor   = SurfaceWhite,
            icon             = { Text("⚠️", fontSize = 28.sp) },
            title = {
                Text("Eliminar cuenta", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            },
            text = {
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
}

// ─── SettingsAccountCard ─────────────────────────────────────────────────────
@Composable
private fun SettingsAccountCard(
    account: Account,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryDark.copy(alpha = 0.6f) else BorderGray,
        label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFE8EDF5) else SurfaceWhite,
        label = "bg"
    )

    Card(
        onClick   = onSelect,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        border    = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, borderColor),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con inicial
            Box(
                modifier        = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) PrimaryDark else BackgroundGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = account.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize  = 18.sp,
                    color     = if (isSelected) Color.White else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = account.name,
                        fontSize   = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color      = TextPrimary
                    )
                    if (account.needsInitialBalance) {
                        Spacer(Modifier.width(6.dp))
                        Text("⚠️", fontSize = 12.sp)
                    }
                }
                Text(
                    text     = if (account.needsInitialBalance) "Saldo inicial pendiente" else account.currency,
                    fontSize = 12.sp,
                    color    = if (account.needsInitialBalance) ExpenseRed else TextSecondary
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PrimaryDark)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Activa", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.width(8.dp))
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp), tint = TextSecondary)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp), tint = ExpenseRed)
            }
        }
    }
}

// ─── Componentes auxiliares ───────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = PrimaryDark)
                Spacer(Modifier.width(4.dp))
                Text(actionLabel, fontSize = 13.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border    = BorderStroke(0.5.dp, BorderGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(icon: String, label: String, value: String) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 18.sp, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Text(text = label, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(text = value, fontSize = 13.sp, color = TextSecondary)
        } else {
            Text("›", fontSize = 18.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun EmptyAccountsCard(onAdd: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border    = BorderStroke(0.5.dp, BorderGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏦", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text("Sin cuentas todavía", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Crea tu primera cuenta para empezar", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onAdd,
                shape   = RoundedCornerShape(10.dp),
                border  = BorderStroke(1.dp, PrimaryDark)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = PrimaryDark)
                Spacer(Modifier.width(6.dp))
                Text("Añadir cuenta", color = PrimaryDark, fontSize = 14.sp)
            }
        }
    }
}
