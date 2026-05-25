package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.core.VersionManager
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.EmergencyFundStatus
import es.aviferdev.n3to.domain.model.HomeBalance
import es.aviferdev.n3to.domain.model.MonthlyGoalProgress
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.component.LockedFeatureOverlay
import es.aviferdev.n3to.ui.common.button.FloatingButtonAdd
import es.aviferdev.n3to.ui.common.component.NavyTabRow
import es.aviferdev.n3to.ui.common.topbar.TopBarWithoutActionsApp
import es.aviferdev.n3to.ui.home.banner.BackupReminderBanner
import es.aviferdev.n3to.ui.home.banner.MaturityReminderBanner
import es.aviferdev.n3to.ui.home.banner.PriceReminderBanner
import es.aviferdev.n3to.ui.home.bottomsheet.AddTransactionBottomSheet
import es.aviferdev.n3to.ui.home.bottomsheet.PriceUpdateBottomSheet
import es.aviferdev.n3to.ui.home.dialog.BackupReminderIntervalDialog
import es.aviferdev.n3to.ui.home.viewmodel.GoalProgressState
import es.aviferdev.n3to.ui.home.viewmodel.HomeUiState
import es.aviferdev.n3to.ui.home.viewmodel.HomeViewModel
import es.aviferdev.n3to.ui.home.viewmodel.NearMaturityState
import es.aviferdev.n3to.ui.home.viewmodel.PriceReminderState
import es.aviferdev.n3to.ui.reconciliation.ReconcileBalanceBottomSheet
import es.aviferdev.n3to.ui.reconciliation.ReconciliationReminderBanner
import es.aviferdev.n3to.ui.settings.backup.BackupPasswordSheet
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.LocalBottomNavPadding
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.version.VersionUpdateBanner
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.home_confirm_identity
import n3to.composeapp.generated.resources.home_section_emergency_fund
import n3to.composeapp.generated.resources.home_section_goals
import n3to.composeapp.generated.resources.home_show_balances
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToCharts: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToFiscalReport: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEmergencyFundSettings: () -> Unit = {},
    onOpenStore: () -> Unit = {},
    onNavigateToFixedIncomeDetail: (String) -> Unit = {},
    onNavigateToCategoryPicker: ((TransactionType) -> Unit)? = null,
    onNavigateToAccountConfig: (String) -> Unit = {},
    reopenFromPicker: Boolean = false,
    onConsumeReopen: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val priceReminder by viewModel.priceReminderState.collectAsState()
    val nearMaturity by viewModel.nearMaturityState.collectAsState()
    val goalProgress by viewModel.goalProgressState.collectAsState()
    val emergencyFund by viewModel.emergencyFundStatus.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val selectedId by viewModel.selectedAccountId.collectAsState()
    val reconciliationState by viewModel.reconciliationUiState.collectAsState()
    val backupSheetState by viewModel.backupSheetState.collectAsState()
    val backupReminderState by viewModel.backupReminderState.collectAsState()
    val versionStatus by viewModel.versionStatus.collectAsState()
    val showAddTransaction by viewModel.showAddTransaction.collectAsState()

    val balancesHidden = LocalBalanceHidden.current
    val showBalancesText = stringResource(Res.string.home_show_balances)
    val confirmIdentityText = stringResource(Res.string.home_confirm_identity)

    LaunchedEffect(reopenFromPicker) {
        if (reopenFromPicker) {
            viewModel.openAddTransaction()
            onConsumeReopen()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> Unit

            is HomeUiState.Success -> {
                val currentAccount = state.balance.selectedAccount
                LaunchedEffect(currentAccount) {
                    currentAccount?.let { viewModel.checkReconciliationReminder(it.id) }
                }

                val versionInfo = (versionStatus as? VersionManager.Status.UpdateAvailable)?.info

                HomeContent(
                    balance = state.balance,
                    categoryNames = state.categoryNames,
                    accounts = accounts,
                    selectedAccountId = selectedId,
                    balancesHidden = balancesHidden,
                    onToggleBalances = {
                        if (balancesHidden) {
                            viewModel.requestShowBalances(showBalancesText, confirmIdentityText)
                        } else {
                            viewModel.hideBalances()
                        }
                    },
                    onAccountSelected = { id -> viewModel.selectAccount(id) },
                    onNavigateToAccountConfig = onNavigateToAccountConfig,
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToCharts = onNavigateToCharts,
                    onNavigateToDebts = onNavigateToDebts,
                    onNavigateToFiscalReport = onNavigateToFiscalReport,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToEmergencyFundSettings = onNavigateToEmergencyFundSettings,
                    onNavigateToFixedIncomeDetail = onNavigateToFixedIncomeDetail,
                    priceReminderState = priceReminder,
                    onUpdateNow = { viewModel.openUpdateSheet() },
                    onRemindLater = { viewModel.dismissReminder() },
                    nearMaturityState = nearMaturity,
                    onDismissNearMaturity = { viewModel.dismissNearMaturityBanner() },
                    showReconciliationBanner = reconciliationState.showBanner,
                    onReconcileNow = { viewModel.openReconciliationSheet(state.balance.selectedAccountBalance) },
                    onReconcileRemindLater = { viewModel.dismissReconciliationBanner() },
                    showBackupBanner = backupReminderState.showBanner,
                    neverBackup = backupReminderState.neverBackup,
                    daysSinceLastBackup = backupReminderState.daysSinceLastBackup,
                    onBackupNow = { viewModel.openBackupExport() },
                    onBackupRemindLater = { viewModel.openBackupIntervalDialog() },
                    showVersionBanner = (versionStatus is VersionManager.Status.UpdateAvailable),
                    versionLatestVersion = versionInfo?.latestVersion,
                    onVersionUpdateNow = onOpenStore,
                    onDismissVersionBanner = { versionInfo?.let { viewModel.dismissVersionBanner(it.latestVersion) } },
                    goalProgressState = goalProgress,
                    emergencyFundStatus = emergencyFund,
                )
            }

            is HomeUiState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.appColors.expense
                )
            }
        }

        FloatingButtonAdd(
            onClick = { viewModel.openAddTransaction() },
            enabled = accounts.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 136.dp)
                .size(52.dp),
        )
    }

    // ── Sheets ────────────────────────────────────────────────────────────────
    if (showAddTransaction) {
        AddTransactionBottomSheet(
            onDismiss = { viewModel.closeAddTransaction() },
            onRequestCategoryPicker = { type ->
                viewModel.closeAddTransaction()
                onNavigateToCategoryPicker?.invoke(type)
            }
        )
    }

    if (priceReminder.showUpdateSheet) {
        PriceUpdateBottomSheet(
            outdatedAssets = priceReminder.outdatedAssets,
            updatedAssetIds = priceReminder.updatedAssetIds,
            onUpdatePrice = { id, price -> viewModel.updateAssetPrice(id, price) },
            onDismiss = { viewModel.closeUpdateSheet() }
        )
    }

    if (reconciliationState.showBottomSheet) {
        ReconcileBalanceBottomSheet(
            state = reconciliationState,
            onRealBalanceChange = { viewModel.updateRealBalance(it) },
            onReconcile = { viewModel.reconcile() },
            onDismiss = { viewModel.closeReconciliationSheet() }
        )
    }

    // ── Backup sheets ────────────────────────────────────────────────────────
    if (backupReminderState.showIntervalDialog) {
        BackupReminderIntervalDialog(
            currentInterval = backupReminderState.currentInterval,
            onIntervalSelected = { days -> viewModel.saveBackupInterval(days) },
            onDismiss = { viewModel.dismissBackupIntervalDialog() }
        )
    }

    if (backupSheetState.action != es.aviferdev.n3to.ui.settings.backup.BackupAction.NONE) {
        BackupPasswordSheet(
            state = backupSheetState,
            onPasswordChange = { viewModel.onBackupPasswordChange(it) },
            onConfirmPasswordChange = { viewModel.onBackupConfirmPasswordChange(it) },
            onConfirm = {
                when (backupSheetState.action) {
                    es.aviferdev.n3to.ui.settings.backup.BackupAction.EXPORT -> viewModel.confirmBackupExport()
                    es.aviferdev.n3to.ui.settings.backup.BackupAction.IMPORT -> viewModel.confirmBackupImport()
                    else -> {}
                }
            },
            onDismiss = {
                viewModel.dismissBackup()
                viewModel.clearBackupResult()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HomeContent — scroll principal
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeContent(
    balance: HomeBalance,
    categoryNames: Map<String, String>,
    accounts: List<Account>,
    selectedAccountId: String?,
    balancesHidden: Boolean,
    onToggleBalances: () -> Unit,
    onAccountSelected: (String) -> Unit,
    onNavigateToAccountConfig: (String) -> Unit = {},
    onNavigateToTransactions: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToFiscalReport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEmergencyFundSettings: () -> Unit = {},
    onNavigateToFixedIncomeDetail: (String) -> Unit = {},
    priceReminderState: PriceReminderState = PriceReminderState(),
    onUpdateNow: () -> Unit = {},
    onRemindLater: () -> Unit = {},
    nearMaturityState: NearMaturityState = NearMaturityState(),
    onDismissNearMaturity: () -> Unit = {},
    showReconciliationBanner: Boolean = false,
    onReconcileNow: () -> Unit = {},
    onReconcileRemindLater: () -> Unit = {},
    showBackupBanner: Boolean = false,
    neverBackup: Boolean = false,
    daysSinceLastBackup: Int = 0,
    onBackupNow: () -> Unit = {},
    onBackupRemindLater: () -> Unit = {},
    showVersionBanner: Boolean = false,
    versionLatestVersion: String? = null,
    onVersionUpdateNow: () -> Unit = {},
    onDismissVersionBanner: () -> Unit = {},
    goalProgressState: GoalProgressState = GoalProgressState(),
    emergencyFundStatus: EmergencyFundStatus = EmergencyFundStatus.NOT_CONFIGURED,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = LocalBottomNavPadding.current)
    ) {
        TopBarWithoutActionsApp(
            onNavigateToSettings = onNavigateToSettings,
            onToggleBalances = onToggleBalances
        )

        if (accounts.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            NavyTabRow(
                items = accounts,
                selected = accounts.find { it.id == selectedAccountId } ?: accounts.first(),
                onSelect = { onAccountSelected(it.id) },
                content = { account ->
                    if (account.needsInitialBalance) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = account.name,
                                fontSize = 12.sp,
                                fontWeight = if (account.id == selectedAccountId) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (account.id == selectedAccountId) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textSecondary
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.appColors.warnAmber
                            )
                        }
                    } else {
                        Text(
                            text = account.name,
                            fontSize = 12.sp,
                            fontWeight = if (account.id == selectedAccountId) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (account.id == selectedAccountId) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textSecondary
                        )
                    }
                },
            )
        }

        Spacer(Modifier.height(14.dp))

        VersionUpdateBanner(
            visible = showVersionBanner,
            latestVersion = versionLatestVersion ?: "",
            onUpdateNow = onVersionUpdateNow,
            onDismiss = onDismissVersionBanner,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (showVersionBanner) Spacer(Modifier.height(8.dp))

        PriceReminderBanner(
            outdatedCount = priceReminderState.outdatedAssets.size,
            visible = priceReminderState.showBanner,
            onUpdateNow = onUpdateNow,
            onRemindLater = onRemindLater,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (priceReminderState.showBanner) Spacer(Modifier.height(8.dp))

        ReconciliationReminderBanner(
            visible = showReconciliationBanner,
            onReconcileNow = onReconcileNow,
            onRemindLater = onReconcileRemindLater,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (showReconciliationBanner) Spacer(Modifier.height(8.dp))

        BackupReminderBanner(
            visible = showBackupBanner,
            neverBackup = neverBackup,
            daysSinceLastBackup = daysSinceLastBackup,
            onBackupClick = onBackupNow,
            onDismiss = onBackupRemindLater,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (showBackupBanner) Spacer(Modifier.height(8.dp))

        MaturityReminderBanner(
            positions = nearMaturityState.positions,
            visible = nearMaturityState.showBanner,
            onDismiss = onDismissNearMaturity,
            onViewDetails = onNavigateToFixedIncomeDetail,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (nearMaturityState.showBanner) Spacer(Modifier.height(8.dp))

        HeroCard(
            balance = balance,
            balancesHidden = balancesHidden,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        SectionHeader(
            label = stringResource(Res.string.home_section_goals),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(4.dp))
        LockedFeatureOverlay(
            locked = accounts.isEmpty(),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            GoalProgressCard(
                progress = goalProgressState.progress ?: MonthlyGoalProgress.from(
                    year = "",
                    month = "",
                    goal = null,
                    savingsActual = 0.0,
                    investmentActual = 0.0
                ),
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))
        SectionHeader(
            label = stringResource(Res.string.home_section_emergency_fund),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(4.dp))
        LockedFeatureOverlay(
            locked = accounts.isEmpty(),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            EmergencyFundCard(
                status = emergencyFundStatus,
                onNavigateToSettings = onNavigateToEmergencyFundSettings,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))
        LockedFeatureOverlay(
            locked = accounts.isEmpty(),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            QuickAccessSection(
                onNavigateToCharts = onNavigateToCharts,
                onNavigateToDebts = onNavigateToDebts,
                onNavigateToFiscalReport = onNavigateToFiscalReport,
                hasDebts = balance.totalOwed > 0 || balance.totalOwing > 0,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))
        LockedFeatureOverlay(
            locked = accounts.isEmpty(),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            RecentTransactionsSection(
                transactions = balance.recentTransactions,
                categoryNames = categoryNames,
                balancesHidden = balancesHidden,
                onVerTodos = onNavigateToTransactions,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
