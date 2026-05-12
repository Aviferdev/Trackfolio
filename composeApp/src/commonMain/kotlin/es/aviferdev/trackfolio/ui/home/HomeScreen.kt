package es.aviferdev.trackfolio.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.AccountType
import es.aviferdev.trackfolio.domain.model.HomeBalance
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.security.BalanceVisibilityManager
import es.aviferdev.trackfolio.security.BiometricAuthenticator
import es.aviferdev.trackfolio.security.BiometricResult
import es.aviferdev.trackfolio.ui.account.AccountSelectorBar
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.reconciliation.ReconcileBalanceBottomSheet
import es.aviferdev.trackfolio.ui.reconciliation.ReconciliationReminderBanner
import es.aviferdev.trackfolio.ui.reconciliation.ReconciliationViewModel
import es.aviferdev.trackfolio.ui.theme.BackgroundGray
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.LocalBalanceHidden
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceElevated
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.formatDate
import es.aviferdev.trackfolio.ui.theme.maskAmount
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
    var showCategoryPicker by remember { mutableStateOf<TransactionType?>(null) }
    val addTransactionViewModel: AddTransactionViewModel = koinViewModel()

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

        // FAB — estilo Revolut: cuadrado redondeado, índigo
        FloatingActionButton(
            onClick = { showAddTransaction = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 112.dp)
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
                showCategoryPicker = type
            },
            viewModel = addTransactionViewModel
        )
    }

    // ── Category Picker overlay (cubre toda la pantalla incluída bottom nav) ──
    showCategoryPicker?.let { type ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(bottom = 64.dp) // espacio para la bottom nav
        ) {
            CategoryPickerScreen(
                initialType = type,
                onBack = { showCategoryPicker = null },
                onCategorySelected = { categoryId ->
                    addTransactionViewModel.onCategoryChange(categoryId)
                    showCategoryPicker = null
                    showAddTransaction = true
                },
                onIncomeTypeSelected = { incomeType ->
                    addTransactionViewModel.onIncomeTypeChange(incomeType)
                    showCategoryPicker = null
                    showAddTransaction = true
                },
                onCreateCategory = { showCategoryPicker = null }
            )
        }
    }

    if (showInitialBalance) {
        SetInitialBalanceBottomSheet(
            accountName = (uiState as? HomeUiState.Success)?.balance?.selectedAccount?.name ?: "",
            currency = (uiState as? HomeUiState.Success)?.balance?.selectedAccount?.currency ?: "€",
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
        val currency =
            (uiState as? HomeUiState.Success)?.balance?.selectedAccount?.currency ?: "€"
        ReconcileBalanceBottomSheet(
            viewModel = reconciliationViewModel,
            currency = currency,
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
                    contentDescription = if (balancesHidden) "Mostrar saldos" else "Ocultar saldos"
                ) {
                    Icon(
                        imageVector = if (balancesHidden) Icons.Outlined.VisibilityOff
                        else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconActionButton(
                    onClick = onNavigateToSettings,
                    contentDescription = "Ajustes"
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
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

// ─────────────────────────────────────────────────────────────────────────────
//  HeroCard
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroCard(
    balance: HomeBalance,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val accountLabel = balance.selectedAccount?.name ?: "Sin cuenta"
    val currency = balance.selectedAccount?.currency ?: "€"
    val netWithDebts = balance.selectedAccountBalance + balance.totalOwed - balance.totalOwing

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 11.dp)
        ) {
            // Nombre de cuenta
            Text(
                text = accountLabel,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.60f),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            // Saldo principal
            Text(
                text = "${
                    maskAmount(
                        formatAmount(balance.selectedAccountBalance),
                        balancesHidden
                    )
                } $currency",
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-2).sp,
                lineHeight = 46.sp
            )

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.12f),
                thickness = 0.5.dp
            )
            Spacer(Modifier.height(14.dp))

            // Neto con deudas
            Text(
                text = "Neto con deudas: ${
                    maskAmount(
                        formatAmount(netWithDebts),
                        balancesHidden
                    )
                } $currency",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.45f)
            )

            Spacer(Modifier.height(12.dp))

            // Me deben / Debo yo — diseño intuitivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DebtChip(
                    label  = "Me deben",
                    amount = "${maskAmount(formatAmount(balance.totalOwed), balancesHidden)} €",
                    color  = IncomeGreen
                )
                DebtChip(
                    label  = "Debo yo",
                    amount = "${maskAmount(formatAmount(balance.totalOwing), balancesHidden)} €",
                    color  = ExpenseRed,
                    alignEnd = true
                )
            }
        }
    }
}

@Composable
private fun DebtChip(
    label: String,
    amount: String,
    color: Color,
    alignEnd: Boolean = false
) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text     = amount,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color    = color
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text      = label,
            fontSize  = 11.sp,
            color     = Color.White.copy(alpha = 0.50f),
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  RecentTransactionsSection
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RecentTransactionsSection(
    transactions: List<Transaction>,
    categoryNames: Map<String, String>,
    balancesHidden: Boolean,
    onVerTodos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Cabecera de sección
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel("Últimos movimientos")
            Text(
                text = "Ver todos",
                fontSize = 12.sp,
                color = PrimaryDark,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onVerTodos() }
            )
        }

        Spacer(Modifier.height(10.dp))

        // Card contenedora
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            if (transactions.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Sin movimientos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Pulsa + para añadir tu primer movimiento",
                            fontSize = 12.sp,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                Column {
                    transactions.forEachIndexed { index, tx ->
                        TransactionRow(
                            transaction = tx,
                            categoryName = resolveTransactionLabel(tx, categoryNames),
                            balancesHidden = balancesHidden
                        )
                        if (index < transactions.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 68.dp),
                                color = BorderGray,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    categoryName: String,
    balancesHidden: Boolean
) {
    val isIncome = transaction.isIncome
    val isAdjustment = transaction.isAdjustment
    val isLinked = transaction.isLinkedToAsset

    val avatarBg = when {
        isAdjustment -> PrimaryDark
        isLinked -> PrimaryDark
        isIncome -> IncomeGreen
        else -> ExpenseRed
    }
    val emoji = when {
        isAdjustment -> null
        isLinked -> null
        isIncome -> transaction.incomeType?.emoji
        else -> null
    }
    val initial = when {
        isAdjustment -> "⚖"
        isLinked -> "📈"
        emoji != null -> emoji
        else -> categoryName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }

    val prefix = when {
        isAdjustment && transaction.amount >= 0 -> "+"
        isAdjustment -> "−"
        isIncome -> "+"
        else -> "−"
    }
    val amountColor = when {
        isAdjustment -> PrimaryDark
        isIncome -> IncomeGreen
        else -> ExpenseRed
    }
    val displayAmount =
        if (isAdjustment) kotlin.math.abs(transaction.amount) else transaction.amount

    val subtitle = when {
        isIncome && transaction.issuerName != null -> transaction.issuerName!!
        else -> formatDate(transaction.date)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Text(initial, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.width(12.dp))

        // Label + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = categoryName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color    = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                fontSize = 11.sp,
                color    = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Importe
        Text(
            text = "$prefix ${maskAmount(formatAmount(displayAmount), balancesHidden)} €",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  QuickAccessSection - 3 cards: Resumen, Deudas, Fiscal
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickAccessSection(
    onNavigateToCharts: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToFiscalReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickCard(
                emoji = "📊",
                label = "Resumen",
                onClick = onNavigateToCharts,
                modifier = Modifier.weight(1f)
            )
            QuickCard(
                emoji = "🤝",
                label = "Deudas",
                onClick = onNavigateToDebts,
                modifier = Modifier.weight(1f)
            )
            QuickCard(
                emoji = "📋",
                label = "Fiscal",
                onClick = onNavigateToFiscalReport,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickCard(
    emoji: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers de UI
// ─────────────────────────────────────────────────────────────────────────────

/** Etiqueta de sección en mayúsculas pequeñas, idéntica a los mocks. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextTertiary,
        letterSpacing = 0.7.sp
    )
}

/** Botón cuadrado/redondeado para acciones de la cabecera. */
@Composable
private fun IconActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    val fakeAccount = Account(
        id = "1",
        name = "Cuenta Corriente",
        currency = "€",
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

/** Resuelve la etiqueta de texto de una transacción (igual que antes). */
private fun resolveTransactionLabel(
    transaction: Transaction,
    categoryNames: Map<String, String>
): String {
    if (transaction.isLinkedToAsset) return transaction.notes ?: "Inversión"
    if (transaction.isAdjustment) return "Ajuste de saldo"
    return if (transaction.isIncome) {
        transaction.incomeType?.label ?: "Ingreso"
    } else {
        transaction.categoryId?.let { categoryNames[it] } ?: "Gasto"
    }
}
