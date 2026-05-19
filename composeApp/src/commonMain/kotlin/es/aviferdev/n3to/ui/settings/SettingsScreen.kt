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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.core.premium.PremiumManager
import es.aviferdev.n3to.core.premium.PremiumStatus
import es.aviferdev.n3to.core.security.AppLockManager
import es.aviferdev.n3to.core.security.ThemeManager
import es.aviferdev.n3to.core.security.BiometricAuthenticator
import es.aviferdev.n3to.core.security.BiometricResult
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.PremiumConstants
import es.aviferdev.n3to.domain.usecase.backup.GetBackupReminderIntervalUseCase
import es.aviferdev.n3to.ui.account.AccountViewModel
import es.aviferdev.n3to.ui.account.AddEditAccountBottomSheet
import es.aviferdev.n3to.ui.common.N3toLabel
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.settings.backup.BackupPasswordSheet
import es.aviferdev.n3to.ui.settings.backup.BackupViewModel
import es.aviferdev.n3to.ui.settings.components.*
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.NavyDeep
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.settings_about
import n3to.composeapp.generated.resources.settings_feedback
import n3to.composeapp.generated.resources.settings_add
import n3to.composeapp.generated.resources.settings_biometric_activate
import n3to.composeapp.generated.resources.settings_biometric_title
import n3to.composeapp.generated.resources.settings_biometric_unavailable
import n3to.composeapp.generated.resources.settings_delete_account_message
import n3to.composeapp.generated.resources.settings_delete_account_title
import n3to.composeapp.generated.resources.settings_not_now
import n3to.composeapp.generated.resources.settings_premium_active
import n3to.composeapp.generated.resources.settings_premium_cta
import n3to.composeapp.generated.resources.settings_premium_lifetime
import n3to.composeapp.generated.resources.settings_premium_limit_message
import n3to.composeapp.generated.resources.settings_premium_limit_title
import n3to.composeapp.generated.resources.settings_premium_title
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
import n3to.composeapp.generated.resources.settings_backup
import n3to.composeapp.generated.resources.home_confirm_identity
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// ─── WRAPPER ────────────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit = {},
    onNavigateToPrivacySettings: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToAccountConfig: (String) -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onResetOnboarding: () -> Unit = {},
    accountViewModel: AccountViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel()
) {
    val accountState by accountViewModel.uiState.collectAsState()
    val selectedId   by accountViewModel.selectedAccountId.collectAsState()
    val backupState  by backupViewModel.state.collectAsState()
    val authenticator: BiometricAuthenticator = koinInject()
    val lockManager: AppLockManager           = koinInject()
    val premiumManager: PremiumManager = koinInject()
    val premiumStatus by premiumManager.status.collectAsState()
    val themeManager: ThemeManager = koinInject()
    val isDarkTheme by themeManager.isDark.collectAsState()

    var biometricEnabled by remember { mutableStateOf(lockManager.biometricEnabled) }
    var biometricError   by remember { mutableStateOf<String?>(null) }
    val biometricTitleText = stringResource(Res.string.settings_biometric_title)
    val acceptText = stringResource(Res.string.common_accept)
    val biometricUnavailableText = stringResource(Res.string.settings_biometric_unavailable)
    val biometricActivateText = stringResource(Res.string.settings_biometric_activate)
    val confirmIdentityText = stringResource(Res.string.home_confirm_identity)

    val backupIntervalUseCase = koinInject<GetBackupReminderIntervalUseCase>()
    var backupInterval by remember { mutableStateOf(backupIntervalUseCase.get()) }

    val handleResetOnboarding: () -> Unit = {
        onResetOnboarding()
    }

    SettingsContent(
        navigateBack = navigateBack,
        backupInterval = backupInterval,
        onBackupIntervalChange = { days ->
            backupInterval = days
            backupIntervalUseCase.set(days)
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
                if (!authenticator.isAvailable()) {
                    biometricError = biometricUnavailableText
                } else {
                    authenticator.authenticate(biometricActivateText, confirmIdentityText) { result ->
                        when (result) {
                            is BiometricResult.Success -> { lockManager.enableBiometric(); biometricEnabled = true }
                            is BiometricResult.Error -> biometricError = result.message
                            else -> {}
                        }
                    }
                }
            } else {
                lockManager.disableBiometric()
                biometricEnabled = false
            }
        },
        onBackupClick = { backupViewModel.openExport() },
        onNavigateToPrivacySettings = onNavigateToPrivacySettings,
        onNavigateToPremium = onNavigateToPremium,
        onNavigateToAccountConfig = onNavigateToAccountConfig,
        onNavigateToFeedback = onNavigateToFeedback,
        onNavigateToAbout = onNavigateToAbout,
        onResetOnboarding = handleResetOnboarding,
        premiumStatus = premiumStatus,
        isDarkTheme = isDarkTheme,
        onToggleTheme = { themeManager.set(!isDarkTheme) }
    )

    // ── Sheets ───────────────────────────────────────────────────────────────
    if (accountState.showAddSheet) {
        AddEditAccountBottomSheet(account = null, onSave = { name -> accountViewModel.addAccount(name) }, onDismiss = { accountViewModel.closeAddSheet() })
    }
    if (accountState.showEditSheet && accountState.editingAccount != null) {
        AddEditAccountBottomSheet(account = accountState.editingAccount, onSave = { name -> accountViewModel.editAccount(accountState.editingAccount!!, name) }, onDismiss = { accountViewModel.closeEditSheet() })
    }
    if (accountState.showDeleteConfirm && accountState.accountToDelete != null) {
        AlertDialog(onDismissRequest = { accountViewModel.cancelDelete() }, containerColor = NavySurface,
            title = { Text(stringResource(Res.string.settings_delete_account_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text  = { Text(stringResource(Res.string.settings_delete_account_message, accountState.accountToDelete!!.name), fontSize = 13.sp, color = TextSecondary) },
            confirmButton = { TextButton(onClick = { accountViewModel.confirmDelete() }) { Text(stringResource(Res.string.common_delete), color = ExpenseRed, fontWeight = FontWeight.SemiBold) } },
            dismissButton = { TextButton(onClick = { accountViewModel.cancelDelete() }) { Text(stringResource(Res.string.common_cancel), color = CyanAccent) } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Mostrar error de biometría si existe
    biometricError?.let { msg ->
        AlertDialog(
            onDismissRequest = { biometricError = null },
            containerColor   = NavySurface,
            title            = { Text(biometricTitleText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text             = { Text(msg, fontSize = 13.sp, color = TextSecondary) },
            confirmButton    = { TextButton(onClick = { biometricError = null }) { Text(acceptText, color = CyanAccent, fontWeight = FontWeight.SemiBold) } },
            shape            = RoundedCornerShape(16.dp)
        )
    }

    // ── Premium limit warning ────────────────────────────────────────────────
    if (accountState.showPremiumLimitWarning) {
        AlertDialog(
            onDismissRequest = { accountViewModel.dismissPremiumLimitWarning() },
            containerColor = NavySurface,
            title = {
                Text(
                    stringResource(Res.string.settings_premium_limit_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    stringResource(Res.string.settings_premium_limit_message, PremiumConstants.MAX_FREE_ACCOUNTS),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    accountViewModel.dismissPremiumLimitWarning()
                    onNavigateToPremium()
                }) {
                    Text(stringResource(Res.string.settings_premium_cta), color = CyanAccent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountViewModel.dismissPremiumLimitWarning() }) {
                    Text(stringResource(Res.string.settings_not_now), color = TextTertiary)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Backup sheet ─────────────────────────────────────────────────────────
    if (backupState.action != es.aviferdev.n3to.ui.settings.backup.BackupAction.NONE) {
        BackupPasswordSheet(
            state = backupState,
            onPasswordChange = { backupViewModel.onPasswordChange(it) },
            onConfirmPasswordChange = { backupViewModel.onConfirmPasswordChange(it) },
            onConfirm = {
                when (backupState.action) {
                    es.aviferdev.n3to.ui.settings.backup.BackupAction.EXPORT -> backupViewModel.confirmExport()
                    es.aviferdev.n3to.ui.settings.backup.BackupAction.IMPORT -> backupViewModel.confirmImport()
                    else -> {}
                }
            },
            onDismiss = {
                backupViewModel.dismiss()
                backupViewModel.clearResult()
            }
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
    onNavigateToPrivacySettings: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateToAccountConfig: (String) -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onResetOnboarding: () -> Unit = {},
    navigateBack: () -> Unit = {},
    backupInterval: Int = 30,
    onBackupIntervalChange: (Int) -> Unit = {},
    onBackupClick: () -> Unit = {},
    premiumStatus: PremiumStatus = PremiumStatus(),
    isDarkTheme: Boolean = true,
    onToggleTheme: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    Column(modifier = modifier.fillMaxSize().background(NavyDeep)) {
        TopBarApp(title = stringResource(Res.string.settings_title), navigateBack = navigateBack)

        AnimatedVisibility(visible = contentVisible, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })) {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_accounts), actionLabel = stringResource(Res.string.settings_add), onAction = onAddAccount)
                }

                if (accounts.isEmpty()) {
                    item { EmptyAccountsCard(onAdd = onAddAccount) }
                } else {
                    items(accounts, key = { it.id }) { account ->
                        SettingsAccountCard(
                            account = account,
                            isSelected = account.id == selectedId,
                            onSelect = { onSelectAccount(account.id) },
                            onEdit = { onEditAccount(account) },
                            onDelete = { onDeleteAccount(account) },
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
                            interval    = backupInterval,
                            onIntervalChange = onBackupIntervalChange
                        )
                    }
                }

                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_appearance))
                    SettingsGroupCard {
                        SettingsThemeRow(isDark = isDarkTheme, onToggle = onToggleTheme)
                    }
                }

                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_privacy))
                    SettingsGroupCard {
                        if (premiumStatus.isPremium) {
                            SettingsInfoRow(
                                label = stringResource(Res.string.settings_premium_title),
                                value = if (premiumStatus.isLifetime) stringResource(Res.string.settings_premium_lifetime) else stringResource(Res.string.settings_premium_active)
                            )
                            SettingsRowDivider()
                        } else {
                            SettingsNavigableRow(
                                icon = Icons.Default.WorkspacePremium,
                                label = stringResource(Res.string.settings_premium_cta),
                                onClick = onNavigateToPremium
                            )
                            SettingsRowDivider()
                        }
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
                        SettingsNavigableRow(icon = Icons.Outlined.SaveAlt, label = stringResource(Res.string.settings_backup),   onClick = onBackupClick)
                    }
                }

                item {
                    SettingsSectionHeader(label = stringResource(Res.string.settings_section_info))
                    SettingsGroupCard {
                        SettingsNavigableRow(icon = Icons.Outlined.Info, label = stringResource(Res.string.settings_about), onClick = onNavigateToAbout)
                        SettingsRowDivider()
                        SettingsNavigableRow(icon = Icons.Outlined.Email, label = stringResource(Res.string.settings_feedback), onClick = onNavigateToFeedback)
                        SettingsRowDivider()
                        SettingsNavigableRow(icon = Icons.Outlined.Refresh, label = stringResource(Res.string.settings_show_onboarding), onClick = onResetOnboarding)
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
                Account(id = "1", name = "Cuenta principal", initialBalance = 1000.0, computedBalance = 1500.0, createdAt = 0L),
                Account(id = "2", name = "Efectivo", initialBalance = 0.0, computedBalance = 500.0, createdAt = 0L)
            ),
            selectedId = "1",
            biometricEnabled = false,
            onAddAccount = {},
            onSelectAccount = {},
            onEditAccount = {},
            onDeleteAccount = {},
            onToggleBiometric = {},
            onNavigateToAbout = {}
        )
    }
}

