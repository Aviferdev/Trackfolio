package es.aviferdev.n3to.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.ui.account.AddEditAccountBottomSheet
import es.aviferdev.n3to.ui.account.AccountViewModel
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountConfigScreen(
    accountId: String,
    onBack: () -> Unit,
    onNavigateToExpenseSettings: () -> Unit = {},
    onNavigateToIncomeSettings: () -> Unit = {},
    onNavigateToTaxProfile: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {}
) {
    val viewModel: AccountConfigViewModel = koinViewModel { parametersOf(accountId) }
    val state by viewModel.uiState.collectAsState()
    val accountVM: AccountViewModel = koinViewModel()

    val account = state.account

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        TopBarApp(
            title = account?.name ?: "Configuración",
            navigateBack = onBack
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
        ) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // — Categorías —
                item {
                    SettingsSectionHeader(label = "Categorías")
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.Outlined.TrendingDown,
                            label = "Categorías de gastos",
                            onClick = onNavigateToExpenseSettings
                        )
                        SettingsRowDivider()
                        SettingsNavigableRow(
                            icon = Icons.Outlined.TrendingUp,
                            label = "Tipos de ingresos",
                            onClick = onNavigateToIncomeSettings
                        )
                    }
                }

                // — Fiscalidad —
                item {
                    SettingsSectionHeader(label = "Fiscalidad")
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.Outlined.AccountBalance,
                            label = "Perfil fiscal",
                            onClick = onNavigateToTaxProfile
                        )
                    }
                }

                // — Planificación —
                item {
                    SettingsSectionHeader(label = "Planificación")
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.Outlined.GpsFixed,
                            label = "Objetivos mensuales",
                            onClick = onNavigateToGoals
                        )
                    }
                }

                // — Mantenimiento —
                item {
                    SettingsSectionHeader(label = "Mantenimiento")
                    SettingsGroupCard {
                        SettingsReconciliationIntervalRow(
                            interval = state.reconciliationInterval,
                            onIntervalChange = { viewModel.updateReconciliationInterval(it) }
                        )
                        SettingsRowDivider()
                        ActionRow(
                            icon = Icons.Outlined.Edit,
                            label = "Editar cuenta",
                            onClick = { viewModel.openEditSheet() },
                            color = PrimaryDark
                        )
                        SettingsRowDivider()
                        ActionRow(
                            icon = Icons.Outlined.Delete,
                            label = "Eliminar cuenta",
                            onClick = { viewModel.requestDelete() },
                            color = ExpenseRed
                        )
                    }
                }
            }
        }
    }

    // — Edit bottom sheet —
    if (viewModel.showEditSheet.collectAsState().value && account != null) {
        AddEditAccountBottomSheet(
            account = account,
            onSave = { newName ->
                accountVM.editAccount(account, newName)
                viewModel.closeEditSheet()
            },
            onDismiss = { viewModel.closeEditSheet() }
        )
    }

    // — Delete confirm —
    if (viewModel.showDeleteConfirm.collectAsState().value) {
        DeleteConfirmDialog(
            title = "Eliminar cuenta",
            message = "¿Eliminar \"${account?.name}\"? Esto también eliminará todos sus movimientos, activos y datos asociados.",
            onConfirm = {
                viewModel.confirmDelete()
                onBack()
            },
            onDismiss = { viewModel.cancelDelete() }
        )
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .then(
                Modifier.let { mod ->
                    // hover/press state would go here in a real impl
                    mod
                }
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.weight(1f)
        )
    }
}
