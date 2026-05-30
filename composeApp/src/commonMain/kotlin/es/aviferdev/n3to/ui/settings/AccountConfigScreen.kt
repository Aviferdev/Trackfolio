package es.aviferdev.n3to.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.account.AddEditAccountBottomSheet
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.common.help.FirstTimeHelpBanner
import es.aviferdev.n3to.ui.common.help.HelpKeys
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.settings.components.SettingsGroupCard
import es.aviferdev.n3to.ui.settings.components.SettingsNavigableRow
import es.aviferdev.n3to.ui.settings.components.SettingsReconciliationIntervalRow
import es.aviferdev.n3to.ui.settings.components.SettingsRowDivider
import es.aviferdev.n3to.ui.settings.components.SettingsSectionHeader
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.coroutines.delay
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.account_archive_anyway
import n3to.composeapp.generated.resources.account_archive_warning_assets_only
import n3to.composeapp.generated.resources.account_archive_warning_both
import n3to.composeapp.generated.resources.account_archive_warning_fi_only
import n3to.composeapp.generated.resources.account_archive_warning_title
import n3to.composeapp.generated.resources.help_account_body
import n3to.composeapp.generated.resources.settings_delete_account_label
import n3to.composeapp.generated.resources.home_section_emergency_fund
import n3to.composeapp.generated.resources.settings_account_section_categories
import n3to.composeapp.generated.resources.settings_account_section_fiscal
import n3to.composeapp.generated.resources.settings_account_section_maintenance
import n3to.composeapp.generated.resources.settings_account_section_planning
import n3to.composeapp.generated.resources.settings_edit_account_label
import n3to.composeapp.generated.resources.settings_expense_categories_label
import n3to.composeapp.generated.resources.settings_income_types_label
import n3to.composeapp.generated.resources.settings_monthly_goals_label
import n3to.composeapp.generated.resources.settings_tax_profile_label
import org.jetbrains.compose.resources.stringResource
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
    val deleted by viewModel.deleted.collectAsState()
    val archiveWarning by viewModel.archiveWarning.collectAsState()

    val account = state.account

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    // Navegar atrás después de eliminar la cuenta
    LaunchedEffect(deleted) {
        if (deleted) onBack()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)) {
        TopBarWithActionsApp(
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
                        key = HelpKeys.ACCOUNT_CONFIG,
                        icon = "ℹ️",
                        label = stringResource(Res.string.help_account_body)
                    )
                }

                // — Categorías —
                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_account_section_categories))
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.AutoMirrored.Outlined.TrendingDown,
                            label = stringResource(Res.string.settings_expense_categories_label),
                            onClick = onNavigateToExpenseSettings
                        )
                        SettingsRowDivider()
                        SettingsNavigableRow(
                            icon = Icons.AutoMirrored.Outlined.TrendingUp,
                            label = stringResource(Res.string.settings_income_types_label),
                            onClick = onNavigateToIncomeSettings
                        )
                    }
                }

                // — Fiscalidad —
                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_account_section_fiscal))
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.Outlined.AccountBalance,
                            label = stringResource(Res.string.settings_tax_profile_label),
                            onClick = onNavigateToTaxProfile
                        )
                    }
                }

                // — Planificación —
                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_account_section_planning))
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.Outlined.GpsFixed,
                            label = stringResource(Res.string.settings_monthly_goals_label),
                            onClick = onNavigateToGoals
                        )
                        SettingsRowDivider()
                        SettingsNavigableRow(
                            icon = Icons.Outlined.Shield,
                            label = stringResource(Res.string.home_section_emergency_fund),
                            onClick = onNavigateToEmergencyFund
                        )
                    }
                }

                // — Mantenimiento —
                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_account_section_maintenance))
                    SettingsGroupCard {
                        SettingsReconciliationIntervalRow(
                            interval = state.reconciliationInterval,
                            onIntervalChange = { viewModel.updateReconciliationInterval(it) }
                        )
                        SettingsRowDivider()
                        ActionRow(
                            icon = Icons.Outlined.Edit,
                            label = stringResource(Res.string.settings_edit_account_label),
                            onClick = { viewModel.openEditSheet() },
                            color = MaterialTheme.appColors.cyanAccent,
                            showChevron = true
                        )
                        SettingsRowDivider()
                        ActionRow(
                            icon = Icons.Outlined.Archive,
                            label = stringResource(Res.string.settings_delete_account_label),
                            onClick = { viewModel.requestDelete() },
                            color = MaterialTheme.appColors.expense
                        )
                    }
                }
            }
        }
    }

    // — Edit bottom sheet (solo editar nombre) —
    if (viewModel.showEditSheet.collectAsState().value && account != null) {
        AddEditAccountBottomSheet(
            account = account,
            onSave = { newName, _, _ -> viewModel.editAccount(account, newName) },
            onDismiss = { viewModel.closeEditSheet() }
        )
    }
    archiveWarning?.let { openItems ->
        val message = when {
            openItems.openAssetsCount > 0 && openItems.openFixedIncomeCount > 0 ->
                stringResource(Res.string.account_archive_warning_both, openItems.openAssetsCount, openItems.openFixedIncomeCount)
            openItems.openAssetsCount > 0 ->
                stringResource(Res.string.account_archive_warning_assets_only, openItems.openAssetsCount)
            else ->
                stringResource(Res.string.account_archive_warning_fi_only, openItems.openFixedIncomeCount)
        }
        DeleteConfirmDialog(
            title = stringResource(Res.string.account_archive_warning_title),
            message = message,
            confirmLabel = stringResource(Res.string.account_archive_anyway),
            onConfirm = { viewModel.confirmArchiveAnyway() },
            onDismiss = { viewModel.dismissArchiveWarning() }
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
