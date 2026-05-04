package es.aviferdev.trackfolio.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.HomeBalance
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.account.AccountSelectorBar
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel()
) {
    val uiState        by viewModel.uiState.collectAsState()
    val accountState   by accountViewModel.uiState.collectAsState()
    val selectedId     by accountViewModel.selectedAccountId.collectAsState()
    var showAddTransaction by remember { mutableStateOf(false) }
    var showInitialBalance by remember { mutableStateOf(false) }

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
                HomeContent(
                    balance = state.balance,
                    categoryNames = state.categoryNames,
                    accounts = accountState.accounts,
                    selectedAccountId = selectedId,
                    onAccountSelected = { id -> accountViewModel.selectAccount(id) },
                    onAddTransaction = { showAddTransaction = true },
                    onNavigateToTransactions = onNavigateToTransactions
                )
            }
        }

        FloatingActionButton(
            onClick = { showAddTransaction = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 32.dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = PrimaryDark,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Text(
                text = "+",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
        }
    }

    if (showAddTransaction) {
        AddTransactionBottomSheet(
            onDismiss = { showAddTransaction = false }
        )
    }

    if (showInitialBalance) {
        SetInitialBalanceBottomSheet(
            accountName = (uiState as? HomeUiState.Success)?.balance?.selectedAccount?.name ?: "",
            currency    = (uiState as? HomeUiState.Success)?.balance?.selectedAccount?.currency ?: "€",
            onDismiss = { /* bloqueado */ },
            onConfirm = { amount ->
                viewModel.setInitialBalance(amount)
                showInitialBalance = false
            }
        )
    }
}

@Composable
private fun HomeContent(
    balance: HomeBalance,
    categoryNames: Map<String, String>,
    accounts: List<es.aviferdev.trackfolio.domain.model.Account>,
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit,
    onAddTransaction: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 16.dp, bottom = 100.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Buenos días", fontSize = 13.sp, color = TextSecondary)
                Text(
                    text = "Trackfolio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .border(0.5.dp, BorderGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("⚙", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Selector de cuentas (si hay más de una o siempre para acceso rápido)
        if (accounts.isNotEmpty()) {
            AccountSelectorBar(
                accounts          = accounts,
                selectedAccountId = selectedAccountId,
                onAccountSelected = onAccountSelected
            )
            Spacer(Modifier.height(8.dp))
        }

        // HeroCard con balance de la cuenta seleccionada
        HeroCard(
            balance  = balance,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(28.dp))

        // Últimos movimientos
        RecentTransactionsSection(
            transactions  = balance.recentTransactions,
            categoryNames = categoryNames,
            onVerTodos    = onNavigateToTransactions,
            modifier      = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(28.dp))

        // Acceso rápido
        QuickAccessSection(modifier = Modifier.padding(horizontal = 20.dp))
    }
}

@Composable
private fun HeroCard(balance: HomeBalance, modifier: Modifier = Modifier) {
    val accountLabel = balance.selectedAccount?.name ?: "Sin cuenta"
    val currency     = balance.selectedAccount?.currency ?: "EUR"

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text     = accountLabel,
                fontSize = 13.sp,
                color    = Color.White.copy(alpha = 0.65f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text          = "${formatAmount(balance.selectedAccountBalance)} $currency",
                fontSize      = 34.sp,
                fontWeight    = FontWeight.Bold,
                color         = Color.White,
                letterSpacing = (-0.5).sp
            )
            // Total global si hay más de una cuenta
            if (balance.totalGlobalBalance != balance.selectedAccountBalance) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = "Total global: ${formatAmount(balance.totalGlobalBalance)} $currency",
                    fontSize = 12.sp,
                    color    = Color.White.copy(alpha = 0.55f)
                )
            }
            if (balance.totalOwed > 0 || balance.totalOwing > 0) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MonthlyIndicator(
                        label      = "Me deben",
                        amount     = balance.totalOwed,
                        isPositive = true,
                        modifier   = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(40.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                            .align(Alignment.CenterVertically)
                    )
                    MonthlyIndicator(
                        label      = "Debo yo",
                        amount     = balance.totalOwing,
                        isPositive = false,
                        modifier   = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyIndicator(
    label: String,
    amount: Double,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isPositive) Color(0xFF66BB6A) else Color(0xFFEF9A9A)
    val arrow = if (isPositive) "↑" else "↓"
    Column(
        modifier            = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = if (isPositive) Alignment.Start else Alignment.End
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = arrow, fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(
                text       = "${formatAmount(amount)} €",
                fontSize   = 13.sp,
                color      = color,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text      = label,
            fontSize  = 11.sp,
            color     = Color.White.copy(alpha = 0.65f),
            textAlign = if (isPositive) TextAlign.Start else TextAlign.End
        )
    }
}

@Composable
private fun RecentTransactionsSection(
    transactions: List<Transaction>,
    categoryNames: Map<String, String>,
    onVerTodos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = "Últimos movimientos",
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
            Text(
                text       = "Ver todos",
                fontSize   = 13.sp,
                color      = PrimaryDark,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.clickable { onVerTodos() }
            )
        }
        Spacer(Modifier.height(12.dp))
        if (transactions.isEmpty()) {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border    = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier        = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = "Sin movimientos",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color      = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text      = "Pulsa + para añadir tu primer movimiento",
                            fontSize  = 13.sp,
                            color     = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border    = CardDefaults.outlinedCardBorder(),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    transactions.forEachIndexed { index, transaction ->
                        TransactionRow(
                            transaction  = transaction,
                            categoryName = categoryNames[transaction.categoryId] ?: transaction.categoryId
                        )
                        if (index < transactions.lastIndex) {
                            HorizontalDivider(
                                modifier  = Modifier.padding(start = 70.dp),
                                color     = BorderGray,
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
private fun TransactionRow(transaction: Transaction, categoryName: String) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isIncome = transaction.type == TransactionType.INCOME
        val bgColor  = if (isIncome) IncomeGreen else ExpenseRed
        val initial  = categoryName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        Box(
            modifier        = Modifier.size(42.dp).clip(CircleShape).background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initial, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = categoryName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(text = formatDate(transaction.date), fontSize = 12.sp, color = TextSecondary)
        }
        val prefix      = if (isIncome) "+" else "−"
        val amountColor = if (isIncome) IncomeGreen else ExpenseRed
        Text(
            text       = "$prefix ${formatAmount(transaction.amount)} €",
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = amountColor
        )
    }
}

@Composable
private fun QuickAccessSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text       = "Acceso rápido",
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAccessCard(label = "Portfolio", icon = "📈", modifier = Modifier.weight(1f))
            QuickAccessCard(label = "Histórico", icon = "🕐", modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickAccessCard(label: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier.aspectRatio(1.2f),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border    = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = label, fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}
