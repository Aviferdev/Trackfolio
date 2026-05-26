package es.aviferdev.n3to.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.ui.account.AddEditAccountBottomSheet
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.settings.backup.BackupAction
import es.aviferdev.n3to.ui.settings.backup.BackupPasswordSheet
import es.aviferdev.n3to.ui.settings.components.EmptyAccountsCard
import es.aviferdev.n3to.ui.settings.components.LanguageSelectorDialog
import es.aviferdev.n3to.ui.settings.components.SettingsAccountCard
import es.aviferdev.n3to.ui.settings.components.SettingsBackupReminderIntervalRow
import es.aviferdev.n3to.ui.settings.components.SettingsBiometricRow
import es.aviferdev.n3to.ui.settings.components.SettingsGroupCard
import es.aviferdev.n3to.ui.settings.components.SettingsLanguageRow
import es.aviferdev.n3to.ui.settings.components.SettingsNavigableRow
import es.aviferdev.n3to.ui.settings.components.SettingsRowDivider
import es.aviferdev.n3to.ui.settings.components.SettingsSectionHeader
import es.aviferdev.n3to.ui.settings.components.SettingsThemeRow
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.coroutines.delay
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.account_archive_anyway
import n3to.composeapp.generated.resources.account_archive_warning_assets_only
import n3to.composeapp.generated.resources.account_archive_warning_both
import n3to.composeapp.generated.resources.account_archive_warning_fi_only
import n3to.composeapp.generated.resources.account_archive_warning_title
import n3to.composeapp.generated.resources.home_confirm_identity
import n3to.composeapp.generated.resources.settings_about
import n3to.composeapp.generated.resources.settings_add
import n3to.composeapp.generated.resources.settings_backup
import n3to.composeapp.generated.resources.settings_biometric_activate
import n3to.composeapp.generated.resources.settings_biometric_title
import n3to.composeapp.generated.resources.settings_biometric_unavailable
import n3to.composeapp.generated.resources.settings_delete_account_message
import n3to.composeapp.generated.resources.settings_delete_account_title
import n3to.composeapp.generated.resources.settings_feedback
import n3to.composeapp.generated.resources.settings_privacy_data
import n3to.composeapp.generated.resources.settings_section_accounts
import n3to.composeapp.generated.resources.settings_section_appearance
import n3to.composeapp.generated.resources.settings_section_data
import n3to.composeapp.generated.resources.settings_section_info
import n3to.composeapp.generated.resources.settings_section_privacy
import n3to.composeapp.generated.resources.settings_section_reminders
import n3to.composeapp.generated.resources.settings_section_security
import n3to.composeapp.generated.resources.settings_show_onboarding
import n3to.composeapp.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

// ─── WRAPPER ────────────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit = {},
    onNavigateToPrivacySettings: () -> Unit = {},
    onNavigateToAccountConfig: (String) -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onResetOnboarding: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val biometricTitleText = stringResource(Res.string.settings_biometric_title)
    val acceptText = stringResource(Res.string.common_accept)
    val biometricUnavailableText = stringResource(Res.string.settings_biometric_unavailable)
    val biometricActivateText = stringResource(Res.string.settings_biometric_activate)
    val confirmIdentityText = stringResource(Res.string.home_confirm_identity)

    SettingsContent(
        navigateBack = navigateBack,
        backupInterval = state.backupInterval,
        onBackupIntervalChange = { viewModel.setBackupInterval(it) },
        accounts = state.accounts,
        selectedId = state.selectedAccountId,
        biometricEnabled = state.biometricEnabled,
        onAddAccount = { viewModel.openAddSheet() },
        onSelectAccount = { viewModel.selectAccount(it) },
        onToggleBiometric = { enabled ->
            viewModel.toggleBiometric(
                enabled,
                biometricUnavailableText,
                biometricActivateText,
                confirmIdentityText
            )
        },
        onBackupClick = { viewModel.openBackupExport() },
        onNavigateToPrivacySettings = onNavigateToPrivacySettings,
        onNavigateToAccountConfig = onNavigateToAccountConfig,
        onNavigateToFeedback = onNavigateToFeedback,
        onNavigateToAbout = onNavigateToAbout,
        onResetOnboarding = onResetOnboarding,
        isDarkTheme = state.isDarkTheme,
        onToggleTheme = { viewModel.toggleTheme() },
        currentLanguage = state.currentLanguage,
        isSystemDefault = state.isSystemDefault,
        onLanguageClick = { viewModel.showLanguageDialog() }
    )

    // ── Add account sheet ────────────────────────────────────────────────────
    if (state.showAddSheet) {
        AddEditAccountBottomSheet(
            account = null,
            onSave = { name, balance -> viewModel.addAccount(name, balance) },
            onDismiss = { viewModel.closeAddSheet() }
        )
    }
    if (state.showEditSheet && state.editingAccount != null) {
        AddEditAccountBottomSheet(
            account = state.editingAccount,
            onSave = { name, _ -> viewModel.editAccount(state.editingAccount!!, name) },
            onDismiss = { viewModel.closeEditSheet() }
        )
    }
    if (state.showDeleteConfirm && state.accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor = MaterialTheme.appColors.navySurface,
            title = {
                Text(
                    stringResource(Res.string.settings_delete_account_title),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(Res.string.settings_delete_account_message, state.accountToDelete!!.name),
                    fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text(
                        stringResource(Res.string.account_archive_anyway),
                        color = MaterialTheme.appColors.expense, fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text(stringResource(Res.string.common_cancel), color = MaterialTheme.appColors.cyanAccent)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    state.archiveWarning?.let { (account, openItems) ->
        val message = when {
            openItems.openAssetsCount > 0 && openItems.openFixedIncomeCount > 0 ->
                stringResource(Res.string.account_archive_warning_both, openItems.openAssetsCount, openItems.openFixedIncomeCount)
            openItems.openAssetsCount > 0 ->
                stringResource(Res.string.account_archive_warning_assets_only, openItems.openAssetsCount)
            else ->
                stringResource(Res.string.account_archive_warning_fi_only, openItems.openFixedIncomeCount)
        }
        AlertDialog(
            onDismissRequest = { viewModel.dismissArchiveWarning() },
            containerColor = MaterialTheme.appColors.navySurface,
            title = {
                Text(
                    stringResource(Res.string.account_archive_warning_title),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = { Text(message, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmArchiveAnyway() }) {
                    Text(
                        stringResource(Res.string.account_archive_anyway),
                        color = MaterialTheme.appColors.expense, fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissArchiveWarning() }) {
                    Text(stringResource(Res.string.common_cancel), color = MaterialTheme.appColors.cyanAccent)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Biometric error ──────────────────────────────────────────────────────
    state.biometricError?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearBiometricError() },
            containerColor = MaterialTheme.appColors.navySurface,
            title = {
                Text(
                    biometricTitleText, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = { Text(msg, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearBiometricError() }) {
                    Text(acceptText, color = MaterialTheme.appColors.cyanAccent, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Language dialog ──────────────────────────────────────────────────────
    if (state.showLanguageDialog) {
        LanguageSelectorDialog(
            currentLanguage = state.currentLanguage,
            isSystemDefault = state.isSystemDefault,
            onLanguageSelected = { viewModel.setLanguage(it) },
            onDismiss = { viewModel.dismissLanguageDialog() }
        )
    }

    // ── Backup sheet ─────────────────────────────────────────────────────────
    val backupSheet = state.backupSheetState
    if (backupSheet.action != BackupAction.NONE) {
        BackupPasswordSheet(
            state = backupSheet,
            onPasswordChange = { viewModel.onBackupPasswordChange(it) },
            onConfirmPasswordChange = { viewModel.onBackupConfirmPasswordChange(it) },
            onConfirm = {
                when (backupSheet.action) {
                    BackupAction.EXPORT -> viewModel.confirmBackupExport()
                    BackupAction.IMPORT -> viewModel.confirmBackupImport()
                    else -> {}
                }
            },
            onDismiss = { viewModel.dismissBackupSheet(); viewModel.clearBackupResult() }
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
    onToggleBiometric: (Boolean) -> Unit,
    onNavigateToPrivacySettings: () -> Unit = {},
    onNavigateToAccountConfig: (String) -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onResetOnboarding: () -> Unit = {},
    navigateBack: () -> Unit = {},
    backupInterval: Int = 30,
    onBackupIntervalChange: (Int) -> Unit = {},
    onBackupClick: () -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleTheme: (Boolean) -> Unit = {},
    currentLanguage: String = "es",
    isSystemDefault: Boolean = true,
    onLanguageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)) {
        TopBarWithActionsApp(
            title = stringResource(Res.string.settings_title),
            navigateBack = navigateBack
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
        ) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    SettingsSectionHeader(
                        label = stringResource(Res.string.settings_section_accounts),
                        actionLabel = stringResource(Res.string.settings_add),
                        onAction = onAddAccount
                    )
                }

                if (accounts.isEmpty()) {
                    item { EmptyAccountsCard(onAdd = onAddAccount) }
                } else {
                    items(accounts, key = { it.id }) { account ->
                        SettingsAccountCard(
                            account = account,
                            isSelected = account.id == selectedId,
                            onSelect = { onSelectAccount(account.id) },
                            onConfigure = { onNavigateToAccountConfig(account.id) }
                        )
                    }
                }

                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_security))
                    SettingsGroupCard {
                        SettingsBiometricRow(enabled = biometricEnabled, onToggle = onToggleBiometric)
                    }
                }

                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_reminders))
                    SettingsGroupCard {
                        SettingsBackupReminderIntervalRow(
                            interval = backupInterval,
                            onIntervalChange = onBackupIntervalChange
                        )
                    }
                }

                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_appearance))
                    SettingsGroupCard {
                        //TODO Comento el tema claro hasta tenerlo solucionado
                        //SettingsThemeRow(isDark = isDarkTheme, onToggle = onToggleTheme)
                        //SettingsRowDivider()
                        SettingsLanguageRow(
                            currentLanguage = currentLanguage,
                            isSystemDefault = isSystemDefault,
                            onClick = onLanguageClick
                        )
                    }
                }

                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_privacy))
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.Outlined.Info,
                            label = stringResource(Res.string.settings_privacy_data),
                            onClick = onNavigateToPrivacySettings
                        )
                    }
                }

                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_data))
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.Outlined.SaveAlt,
                            label = stringResource(Res.string.settings_backup),
                            onClick = onBackupClick
                        )
                    }
                }

                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_info))
                    SettingsGroupCard {
                        SettingsNavigableRow(
                            icon = Icons.Outlined.Info,
                            label = stringResource(Res.string.settings_about),
                            onClick = onNavigateToAbout
                        )
                        SettingsRowDivider()
                        SettingsNavigableRow(
                            icon = Icons.Outlined.Email,
                            label = stringResource(Res.string.settings_feedback),
                            onClick = onNavigateToFeedback
                        )
                        SettingsRowDivider()
                        SettingsNavigableRow(
                            icon = Icons.Outlined.Refresh,
                            label = stringResource(Res.string.settings_show_onboarding),
                            onClick = onResetOnboarding
                        )
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
    N3toTheme {
        SettingsContent(
            accounts = listOf(
                Account(
                    id = "1", name = "Cuenta principal",
                    initialBalance = 1000.0, computedBalance = 1500.0, createdAt = 0L
                ),
                Account(
                    id = "2", name = "Efectivo",
                    initialBalance = 0.0, computedBalance = 500.0, createdAt = 0L
                )
            ),
            selectedId = "1",
            biometricEnabled = false,
            onAddAccount = {},
            onSelectAccount = {},
            onToggleBiometric = {},
            onNavigateToAbout = {}
        )
    }
}
