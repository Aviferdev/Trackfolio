package es.aviferdev.trackfolio.ui.home

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
import androidx.compose.material3.Icon
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
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.AccountType
import es.aviferdev.trackfolio.domain.model.HomeBalance
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.core.security.BalanceVisibilityManager
import es.aviferdev.trackfolio.core.security.BiometricAuthenticator
import es.aviferdev.trackfolio.core.security.BiometricResult
import es.aviferdev.trackfolio.ui.account.AccountSelectorBar
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.common.component.IconActionButton
import es.aviferdev.trackfolio.ui.reconciliation.ReconcileBalanceBottomSheet
import es.aviferdev.trackfolio.ui.reconciliation.ReconciliationReminderBanner
import es.aviferdev.trackfolio.ui.reconciliation.ReconciliationViewModel
import es.aviferdev.trackfolio.ui.theme.BackgroundGray
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.LocalBalanceHidden
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

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
    onNavigateToCategoryPicker: ((TransactionType) -> Unit)? = null,
    reopenFromPicker: Boolean = false,
    onConsumeReopen: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel(),
    reconciliationViewModel: ReconciliationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val priceReminder by viewModel.priceReminderState.collectAsState()
    val nearMaturity by viewModel.nearMaturityState.collectAsState()
    val accountState by accountViewModel.uiState.collectAsState()
    val selectedId by accountViewModel.selectedAccountId.collectAsState()
    val reconciliationState by reconciliationViewModel.uiState.collectAsState()
    val balanceVisibility = koinInject<BalanceVisibilityManager>()
    val authenticator: BiometricAuthenticator = koinInject()
    val balancesHidden = LocalBalanceHidden.current

    var showAddTransaction by remember { mutableStateOf(false) }
    var showInitialBalance by remember { mutableStateOf(false) }
    val addTransactionViewModel: AddTransactionViewModel = koinViewModel()

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
            .background(BackgroundGray)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryDark
                )
            }

            is HomeUiState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center),
                    color = ExpenseRed
                )
            }

            is HomeUiState.Success -> {
                if (state.showInitialBalancePrompt && !showAddTransaction) {
                    LaunchedEffect(state) { showInitialBalance = true }
                }
                val currentAccount = state.balance.selectedAccount
                LaunchedEffect(currentAccount) {
                    if (currentAccount?.isCash == true) reconciliationViewModel.checkReminder()
                }

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
                                    "Mostrar saldos",
                                    "Confirma tu identidad"
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
                    priceReminderState = priceReminder,
                    onUpdateNow = { viewModel.openUpdateSheet() },
                    onRemindLater = { viewModel.dismissReminder() },
                    nearMaturityState = nearMaturity,
                    onDismissNearMaturity = { viewModel.dismissNearMaturityBanner() },
                    showReconciliationBanner = reconciliationState.showBanner && currentAccount?.isCash == true,
                    onReconcileNow = { reconciliationViewModel.openBottomSheet(state.balance.selectedAccountBalance) },
                    onReconcileRemindLater = { reconciliationViewModel.dismissBanner() }
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
            containerColor = PrimaryDark,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = Color.White)
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
}

// ─────────────────────────────────────────────────────────────────────────────
//  HomeContent — scroll principal
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeContent(
    balance: HomeBalance,
    categoryNames: Map<String, String>,
    accounts: List<es.aviferdev.trackfolio.domain.model.Account>,
    selectedAccountId: String?,
    balancesHidden: Boolean,
    onToggleBalances: () -> Unit,
    onAccountSelected: (String) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToFiscalReport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    priceReminderState: PriceReminderState = PriceReminderState(),
    onUpdateNow: () -> Unit = {},
    onRemindLater: () -> Unit = {},
    nearMaturityState: NearMaturityState = NearMaturityState(),
    onDismissNearMaturity: () -> Unit = {},
    showReconciliationBanner: Boolean = false,
    onReconcileNow: () -> Unit = {},
    onReconcileRemindLater: () -> Unit = {}
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
                    text = "Trackfolio",
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
                    label = if (balancesHidden) "Mostrar saldos" else "Ocultar saldos"
                )
                IconActionButton(
                    onClick = onNavigateToSettings,
                    icon = Icons.Outlined.Settings,
                    iconTint = TextSecondary,
                    label = "Ajustes"
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

        MaturityReminderBanner(
            positions = nearMaturityState.positions,
            visible = nearMaturityState.showBanner,
            onDismiss = onDismissNearMaturity,
            onViewDetails = { /* TODO: navigate to fixed income detail */ },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (nearMaturityState.showBanner) Spacer(Modifier.height(8.dp))

        // ── Hero card ─────────────────────────────────────────────────────────
        HeroCard(
            balance = balance,
            balancesHidden = balancesHidden,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // ── Acceso rápido ─────────────────────────────────────────────────────
        Spacer(Modifier.height(24.dp))
        QuickAccessSection(
            onNavigateToCharts = onNavigateToCharts,
            onNavigateToDebts = onNavigateToDebts,
            onNavigateToFiscalReport = onNavigateToFiscalReport,
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
        createdAt = 0L,
        accountType = AccountType.GENERAL
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
            irpfPercent = 19.0,
            socialSecurityAmount = 250.0,
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

    TrackfolioTheme {
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
    val now = kotlinx.datetime.Clock.System.now()
    val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = local.hour
    return when {
        hour in 6..11  -> "Buenos días"
        hour in 12..19 -> "Buenas tardes"
        else           -> "Buenas noches"
    }
}
