package es.aviferdev.n3to.ui.home

import es.aviferdev.n3to.platform.nowHour
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.core.VersionManager
import es.aviferdev.n3to.core.security.BalanceVisibilityManager
import es.aviferdev.n3to.core.security.BiometricAuthenticator
import es.aviferdev.n3to.core.security.BiometricResult
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.CategoryBudgetStatus
import es.aviferdev.n3to.domain.model.EmergencyFundStatus
import es.aviferdev.n3to.domain.model.HomeBalance
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.model.MonthlyGoalProgress
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.backup.GetBackupReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.backup.GetLastBackupDateUseCase
import es.aviferdev.n3to.domain.usecase.backup.SaveBackupReminderDismissedUseCase
import es.aviferdev.n3to.domain.usecase.backup.SaveBackupReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.backup.ShouldShowBackupReminderUseCase
import es.aviferdev.n3to.ui.account.AccountSelectorBar
import es.aviferdev.n3to.ui.account.AccountViewModel
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.component.IconActionButton
import es.aviferdev.n3to.ui.reconciliation.ReconcileBalanceBottomSheet
import es.aviferdev.n3to.ui.reconciliation.ReconciliationReminderBanner
import es.aviferdev.n3to.ui.reconciliation.ReconciliationViewModel
import es.aviferdev.n3to.ui.settings.SetCategoryLimitSheet
import es.aviferdev.n3to.ui.settings.backup.BackupPasswordSheet
import es.aviferdev.n3to.ui.settings.backup.BackupViewModel
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.NavyDeep
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.version.VersionUpdateBanner
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.greeting_afternoon
import n3to.composeapp.generated.resources.greeting_evening
import n3to.composeapp.generated.resources.greeting_morning
import n3to.composeapp.generated.resources.home_confirm_identity
import n3to.composeapp.generated.resources.home_hide_balances
import n3to.composeapp.generated.resources.home_show_balances
import n3to.composeapp.generated.resources.home_section_emergency_fund
import n3to.composeapp.generated.resources.home_section_goals
import n3to.composeapp.generated.resources.home_settings_cd
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.qualifier.named


// ─────────────────────────────────────────────────────────────────────────────
//  HomeScreen — entry point (sin cambios de lógica/VM)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToCharts: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToFiscalReport: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToExpenseSettings: () -> Unit = {},
    onNavigateToEmergencyFundSettings: () -> Unit = {},
    onNavigateToFixedIncomeDetail: (String) -> Unit = {},
    onNavigateToCategoryPicker: ((TransactionType) -> Unit)? = null,
    reopenFromPicker: Boolean = false,
    onConsumeReopen: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel(),
    reconciliationViewModel: ReconciliationViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val priceReminder by viewModel.priceReminderState.collectAsState()
    val nearMaturity by viewModel.nearMaturityState.collectAsState()
    val goalProgress by viewModel.goalProgressState.collectAsState()
    val emergencyFund by viewModel.emergencyFundStatus.collectAsState()
    val budgetStatus by viewModel.budgetStatus.collectAsState()

    val accountState by accountViewModel.uiState.collectAsState()
    val selectedId by accountViewModel.selectedAccountId.collectAsState()
    val reconciliationState by reconciliationViewModel.uiState.collectAsState()
    val backupSheetState by backupViewModel.state.collectAsState()
    val versionStatus by viewModel.versionStatus.collectAsState()
    val openStore: () -> Unit = koinInject(named("openStore"))
    val balanceVisibility = koinInject<BalanceVisibilityManager>()
    val authenticator: BiometricAuthenticator = koinInject()
    val balancesHidden = LocalBalanceHidden.current
    val showBalancesText = stringResource(Res.string.home_show_balances)
    val confirmIdentityText = stringResource(Res.string.home_confirm_identity)

    // ── Backup reminder state ────────────────────────────────────────────────
    val shouldShowBackupReminder = koinInject<ShouldShowBackupReminderUseCase>()
    val getLastBackupDate = koinInject<GetLastBackupDateUseCase>()
    val getBackupReminderInterval = koinInject<GetBackupReminderIntervalUseCase>()
    val saveBackupReminderInterval = koinInject<SaveBackupReminderIntervalUseCase>()
    val saveBackupReminderDismissed = koinInject<SaveBackupReminderDismissedUseCase>()

    var showBackupBanner by remember { mutableStateOf(false) }
    var neverBackup by remember { mutableStateOf(false) }
    var daysSinceLastBackup by remember { mutableStateOf(0) }
    var showBackupIntervalDialog by remember { mutableStateOf(false) }

    // Comprobar si debe mostrarse el banner de backup al iniciar
    LaunchedEffect(Unit) {
        val shouldShow = shouldShowBackupReminder()
        if (shouldShow) {
            val lastBackupMillis = getLastBackupDate()
            if (lastBackupMillis == 0L) {
                neverBackup = true
                daysSinceLastBackup = 0
            } else {
                neverBackup = false
                val now = nowMillis()
                val diffDays = ((now - lastBackupMillis) / (24 * 60 * 60 * 1000)).toInt()
                daysSinceLastBackup = maxOf(diffDays, 1)
            }
            showBackupBanner = true
        }
    }

    var showAddTransaction by remember { mutableStateOf(false) }
    var showInitialBalance by remember { mutableStateOf(false) }
    val addTransactionViewModel: AddTransactionViewModel = koinViewModel()

    // ── Estado del limit sheet inline en Home ──────────────────────────────────
    var showBudgetLimitSheet by remember { mutableStateOf(false) }
    var budgetLimitCategoryId by remember { mutableStateOf("") }
    var budgetLimitCategoryName by remember { mutableStateOf("") }
    var budgetLimitCurrentLimit by remember { mutableStateOf(0.0) }
    var budgetLimitCurrentType by remember { mutableStateOf(LimitType.FIXED) }

    // Reabrir sheet al volver del CategoryPicker
    LaunchedEffect(reopenFromPicker) {
        if (reopenFromPicker) {
            showAddTransaction = true
            onConsumeReopen()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CyanAccent
                )
            }

            is HomeUiState.Success -> {
                if (state.showInitialBalancePrompt && !showAddTransaction) {
                    LaunchedEffect(state) { showInitialBalance = true }
                }
                val currentAccount = state.balance.selectedAccount
                LaunchedEffect(currentAccount) {
                    currentAccount?.let { reconciliationViewModel.checkReminder(it.id) }
                }

                val showVersionBanner = versionStatus is VersionManager.Status.UpdateAvailable
                val versionInfo = (versionStatus as? VersionManager.Status.UpdateAvailable)?.info

                HomeContent(
                    balance = state.balance,
                    categoryNames = state.categoryNames,
                    accounts = accountState.accounts,
                    selectedAccountId = selectedId,
                    balancesHidden = balancesHidden,
                    onToggleBalances = {
                        if (balancesHidden) {
                            balanceVisibility.requestShow {
                                authenticator.authenticate(
                                    showBalancesText,
                                    confirmIdentityText
                                ) { result ->
                                    when (result) {
                                        is BiometricResult.Success -> balanceVisibility.onBiometricSuccess()
                                        is BiometricResult.UserCancelled -> Unit
                                        else -> Unit
                                    }
                                }
                            }
                        } else {
                            balanceVisibility.hide()
                        }
                    },
                    onAccountSelected = { id -> accountViewModel.selectAccount(id) },
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
                    onReconcileNow = { reconciliationViewModel.openBottomSheet(state.balance.selectedAccountBalance) },
                    onReconcileRemindLater = { reconciliationViewModel.dismissBanner() },
                    showBackupBanner = showBackupBanner,
                    neverBackup = neverBackup,
                    daysSinceLastBackup = daysSinceLastBackup,
                    onBackupNow = { backupViewModel.openExport() },
                    onBackupRemindLater = { showBackupIntervalDialog = true },
                    showVersionBanner = showVersionBanner,
                    versionLatestVersion = versionInfo?.latestVersion,
                    onVersionUpdateNow = openStore,
                    onDismissVersionBanner = { versionInfo?.let { viewModel.dismissVersionBanner(it.latestVersion) } },
                    goalProgressState = goalProgress,
                    emergencyFundStatus = emergencyFund,
                    budgetStatus = budgetStatus,
                    onNavigateToExpenseSettings = onNavigateToExpenseSettings,
                    onEditBudget = { catId, catName, currentLimit, limitType ->
                        budgetLimitCategoryId = catId
                        budgetLimitCategoryName = catName
                        budgetLimitCurrentLimit = currentLimit
                        budgetLimitCurrentType = limitType
                        showBudgetLimitSheet = true
                    }
                )
            }

            is HomeUiState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center),
                    color = ExpenseRed
                )
            }
        }

        FloatingActionButton(
            onClick = { showAddTransaction = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 136.dp)
                .size(52.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = NavySurface,
            contentColor = CyanAccent,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            )
        ) {
            Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = CyanAccent)
        }
    }

    // ── Sheets ────────────────────────────────────────────────────────────────
    if (showAddTransaction) {
        AddTransactionBottomSheet(
            onDismiss = { showAddTransaction = false },
            onRequestCategoryPicker = { type ->
                showAddTransaction = false
                onNavigateToCategoryPicker?.invoke(type)
            },
            viewModel = addTransactionViewModel
        )
    }

    if (showInitialBalance) {
        SetInitialBalanceBottomSheet(
            accountName = (uiState as? HomeUiState.Success)?.balance?.selectedAccount?.name ?: "",
            onConfirm = { amount ->
                viewModel.setInitialBalance(amount); showInitialBalance = false
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
            viewModel = reconciliationViewModel,
            onDismiss = { reconciliationViewModel.closeBottomSheet() }
        )
    }

    // ── Backup sheets ────────────────────────────────────────────────────────
    if (showBackupIntervalDialog) {
        BackupReminderIntervalDialog(
            currentInterval = getBackupReminderInterval.get(),
            onIntervalSelected = { days ->
                saveBackupReminderInterval(days)
                saveBackupReminderDismissed()
                showBackupIntervalDialog = false
                showBackupBanner = false
            },
            onDismiss = { showBackupIntervalDialog = false }
        )
    }

    if (backupSheetState.action != es.aviferdev.n3to.ui.settings.backup.BackupAction.NONE) {
        BackupPasswordSheet(
            state = backupSheetState,
            onPasswordChange = { backupViewModel.onPasswordChange(it) },
            onConfirmPasswordChange = { backupViewModel.onConfirmPasswordChange(it) },
            onConfirm = {
                when (backupSheetState.action) {
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

    // ── Limit sheet inline ─────────────────────────────────────────────────────
    if (showBudgetLimitSheet) {
        SetCategoryLimitSheet(
            categoryName = budgetLimitCategoryName,
            currentLimit = budgetLimitCurrentLimit,
            currentLimitType = budgetLimitCurrentType,
            onSave = { limit, limitType ->
                viewModel.saveBudgetLimit(budgetLimitCategoryId, limit, limitType)
                showBudgetLimitSheet = false
            },
            onDismiss = { showBudgetLimitSheet = false }
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
    budgetStatus: List<CategoryBudgetStatus> = emptyList(),
    onNavigateToExpenseSettings: () -> Unit = {},
    onEditBudget: (categoryId: String, categoryName: String, currentLimit: Double, currentLimitType: LimitType) -> Unit = { _, _, _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 100.dp)
    ) {
        // ── Cabecera ──────────────────────────────────────────────────────────
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val greeting = getGreeting()
                Text(
                    text = greeting,
                    fontSize = 12.sp,
                    color = TextTertiary,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "N3to",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconActionButton(
                    onClick = onToggleBalances,
                    icon = if (balancesHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    iconTint = TextSecondary,
                    label = if (balancesHidden) stringResource(Res.string.home_show_balances) else stringResource(Res.string.home_hide_balances)
                )
                IconActionButton(
                    onClick = onNavigateToSettings,
                    icon = Icons.Outlined.Settings,
                    iconTint = TextSecondary,
                    label = stringResource(Res.string.home_settings_cd)
                )
            }
        }

        // ── Selector de cuentas ───────────────────────────────────────────────
        if (accounts.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            AccountSelectorBar(
                accounts = accounts,
                selectedAccountId = selectedAccountId,
                onAccountSelected = onAccountSelected
            )
        }

        Spacer(Modifier.height(14.dp))

        // ── Banners contextuales ──────────────────────────────────────────────
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

        // ── Hero card ─────────────────────────────────────────────────────────
        HeroCard(
            balance = balance,
            balancesHidden = balancesHidden,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // ── Progreso de objetivos ─────────────────────────────────────────────
        Spacer(Modifier.height(24.dp))
        SectionHeader(
            label = stringResource(Res.string.home_section_goals),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))
        GoalProgressCard(
            progress = goalProgressState.progress ?: MonthlyGoalProgress.from(
                year = "",
                month = "",
                goal = null,
                savingsActual = 0.0,
                investmentActual = 0.0
            ),
            onNavigateToSettings = onNavigateToSettings,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // ── Fondo de emergencia ───────────────────────────────────────────────
        Spacer(Modifier.height(24.dp))
        SectionHeader(
            label = stringResource(Res.string.home_section_emergency_fund),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))
        EmergencyFundCard(
            status = emergencyFundStatus,
            onNavigateToSettings = onNavigateToEmergencyFundSettings,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // ── Presupuestos ──────────────────────────────────────────────────────
        Spacer(Modifier.height(24.dp))
        SectionHeader(
            label = "PRESUPUESTOS",
            actionLabel = if (budgetStatus.isNotEmpty()) "Editar" else null,
            onAction = if (budgetStatus.isNotEmpty()) onNavigateToExpenseSettings else null,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(10.dp))
        BudgetSection(
            statuses = budgetStatus,
            onEditBudget = onEditBudget,
            onConfigureBudgets = onNavigateToExpenseSettings,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // ── Acceso rápido ─────────────────────────────────────────────────────
        Spacer(Modifier.height(24.dp))
        QuickAccessSection(
            onNavigateToCharts = onNavigateToCharts,
            onNavigateToDebts = onNavigateToDebts,
            onNavigateToFiscalReport = onNavigateToFiscalReport,
            hasDebts = balance.totalOwed > 0 || balance.totalOwing > 0,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // ── Últimos movimientos ───────────────────────────────────────────────
        Spacer(Modifier.height(24.dp))
        RecentTransactionsSection(
            transactions = balance.recentTransactions,
            categoryNames = categoryNames,
            balancesHidden = balancesHidden,
            onVerTodos = onNavigateToTransactions,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// HeroCard, QuickAccessSection y RecentTransactionsSection
// extraídos a archivos propios (HeroCard.kt, QuickAccessSection.kt, RecentTransactionsSection.kt)

@Preview
@Composable
private fun HomeContentPreview() {
    val fakeAccount = Account(
        id = "1",
        name = "Cuenta Corriente",
        initialBalance = 1000.0,
        computedBalance = 3500.0,
        createdAt = 0L
    )
    val fakeTransactions = listOf(
        Transaction(
            id = "1",
            accountId = "1",
            amount = 2500.0,
            type = TransactionType.INCOME,
            categoryId = null,
            date = 1715500800000L,
            notes = null,
            createdAt = 1715500800000L,
            incomeType = IncomeType.SALARY,
            grossAmount = 3000.0,
            issuerName = "Empresa S.L."
        ),
        Transaction(
            id = "2",
            accountId = "1",
            amount = 85.50,
            type = TransactionType.EXPENSE,
            categoryId = "food",
            date = 1715414400000L,
            notes = null,
            createdAt = 1715414400000L
        ),
        Transaction(
            id = "3",
            accountId = "1",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            categoryId = "transport",
            date = 1715328000000L,
            notes = null,
            createdAt = 1715328000000L
        )
    )
    val fakeBalance = HomeBalance(
        selectedAccount = fakeAccount,
        selectedAccountBalance = 3500.0,
        totalOwed = 500.0,
        totalOwing = 200.0,
        recentTransactions = fakeTransactions
    )
    val fakeCategoryNames = mapOf(
        "food" to "Alimentación",
        "transport" to "Transporte"
    )

    N3toTheme {
        HomeContent(
            balance = fakeBalance,
            categoryNames = fakeCategoryNames,
            accounts = listOf(fakeAccount),
            selectedAccountId = "1",
            balancesHidden = false,
            onToggleBalances = {},
            onAccountSelected = {},
            onNavigateToTransactions = {},
            onNavigateToCharts = {},
            onNavigateToDebts = {},
            onNavigateToFiscalReport = {},
            onNavigateToSettings = {}
        )
    }
}

/** Saludo según la hora del día. */
private fun getGreeting(): String {
    val hour = nowHour()
    return when {
        hour in 6..11  -> "Buenos días"
        hour in 12..19 -> "Buenas tardes"
        else           -> "Buenas noches"
    }
}
