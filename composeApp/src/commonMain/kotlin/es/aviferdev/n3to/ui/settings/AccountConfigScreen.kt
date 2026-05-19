package es.aviferdev.n3to.ui.settings

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.settings.components.*
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.ui.account.AddEditAccountBottomSheet
import es.aviferdev.n3to.ui.account.AccountViewModel
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.common.help.FirstTimeHelpBanner
import es.aviferdev.n3to.ui.common.help.HelpContent
import es.aviferdev.n3to.ui.common.help.HelpKeys
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
    onNavigateToGoals: () -> Unit = {},
    onNavigateToEmergencyFund: () -> Unit = {}
) {
    val viewModel: AccountConfigViewModel = koinViewModel { parametersOf(accountId) }
    val state by viewModel.uiState.collectAsState()
    val accountVM: AccountViewModel = koinViewModel()

    val account = state.account

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)) {
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
                // — Ayuda contextual —
                item {
                    FirstTimeHelpBanner(
                        key   = HelpKeys.ACCOUNT_CONFIG,
                        icon  = "ℹ️",
                        label = HelpContent.texts[HelpKeys.ACCOUNT_CONFIG]?.body ?: ""
                    )
                }

                // — Categorías —
                item {
                    SettingsSectionHeader(label = "Categorías")
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.AutoMirrored.Outlined.TrendingDown,
                            label = "Categorías de gastos",
                            onClick = onNavigateToExpenseSettings
                        )
                        SettingsRowDivider()
                        SettingsNavigableRow(
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
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
                        SettingsRowDivider()
                        SettingsNavigableRow(
                            icon = Icons.Outlined.Shield,
                            label = "Fondo de emergencia",
                            onClick = onNavigateToEmergencyFund
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
                            color = MaterialTheme.appColors.cyanAccent,
                            showChevron = true
                        )
                        SettingsRowDivider()
                        ActionRow(
                            icon = Icons.Outlined.Delete,
                            label = "Eliminar cuenta",
                            onClick = { viewModel.requestDelete() },
                            color = MaterialTheme.appColors.expense
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
            onSave = { newName, _ ->
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
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color,
    showChevron: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.appColors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
