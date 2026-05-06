package es.aviferdev.trackfolio.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.HomeBalance
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.security.BalanceVisibilityManager
import es.aviferdev.trackfolio.ui.account.AccountSelectorBar
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.theme.BackgroundGray
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.LocalBalanceHidden
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.formatDate
import es.aviferdev.trackfolio.ui.theme.maskAmount
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToCharts: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel()
) {
    val uiState        by viewModel.uiState.collectAsState()
    val accountState   by accountViewModel.uiState.collectAsState()
    val selectedId     by accountViewModel.selectedAccountId.collectAsState()
    val balanceVisibility = koinInject<BalanceVisibilityManager>()
    val balancesHidden = LocalBalanceHidden.current
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
                    balancesHidden = balancesHidden,
                    onToggleBalances = { balanceVisibility.toggle() },
                    onAccountSelected = { id -> accountViewModel.selectAccount(id) },
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToCharts = onNavigateToCharts,
                    onNavigateToSettings = onNavigateToSettings
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
    balancesHidden: Boolean,
    onToggleBalances: () -> Unit,
    onAccountSelected: (String) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 100.dp)
    ) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(0.5.dp, BorderGray, CircleShape)
                        .clickable { onToggleBalances() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (balancesHidden) Icons.Outlined.VisibilityOff
                                      else Icons.Outlined.Visibility,
                        contentDescription = if (balancesHidden) "Mostrar saldos"
                                              else "Ocultar saldos"
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(0.5.dp, BorderGray, CircleShape)
                        .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Settings, "Ajustes")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (accounts.isNotEmpty()) {
            AccountSelectorBar(
                accounts          = accounts,
                selectedAccountId = selectedAccountId,
                onAccountSelected = onAccountSelected
            )
            Spacer(Modifier.height(8.dp))
        }

        HeroCard(
            balance        = balance,
            balancesHidden = balancesHidden,
            modifier       = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(28.dp))

        RecentTransactionsSection(
            transactions   = balance.recentTransactions,
            categoryNames  = categoryNames,
            balancesHidden = balancesHidden,
            onVerTodos     = onNavigateToTransactions,
            modifier       = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(28.dp))

        QuickAccessSection(
            onNavigateToCharts = onNavigateToCharts,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun HeroCard(balance: HomeBalance, balancesHidden: Boolean, modifier: Modifier = Modifier) {
    val accountLabel = balance.selectedAccount?.name ?: "Sin cuenta"
    val currency     = balance.selectedAccount?.currency ?: "EUR"

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = PrimaryDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(text = accountLabel, fontSize = 13.sp, color = Color.White.copy(alpha = 0.65f))
            Spacer(Modifier.height(8.dp))
            Text(
                text          = "${maskAmount(formatAmount(balance.selectedAccountBalance), balancesHidden)} $currency",
                fontSize      = 34.sp,
                fontWeight    = FontWeight.Bold,
                color         = Color.White,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            val netWithDebts = balance.selectedAccountBalance + balance.totalOwed - balance.totalOwing
            Text(
                text     = "Neto con deudas: ${maskAmount(formatAmount(netWithDebts), balancesHidden)} $currency",
                fontSize = 11.sp,
                color    = Color.White.copy(alpha = 0.50f)
            )

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                MonthlyIndicator("Me deben", balance.totalOwed, true, balancesHidden, Modifier.weight(1f))
                Box(
                    modifier = Modifier.width(0.5.dp).height(40.dp).background(Color.White.copy(alpha = 0.15f)).align(Alignment.CenterVertically)
                )
                MonthlyIndicator("Debo yo", balance.totalOwing, false, balancesHidden, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MonthlyIndicator(label: String, amount: Double, isPositive: Boolean, balancesHidden: Boolean, modifier: Modifier = Modifier) {
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
                text       = "${maskAmount(formatAmount(amount), balancesHidden)} €",
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
    balancesHidden: Boolean,
    onVerTodos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Últimos movimientos", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("Ver todos", fontSize = 13.sp, color = PrimaryDark, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onVerTodos() })
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
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sin movimientos", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("Pulsa + para añadir tu primer movimiento", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
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
                            transaction    = transaction,
                            categoryName   = resolveTransactionLabel(transaction, categoryNames),
                            balancesHidden = balancesHidden
                        )
                        if (index < transactions.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(start = 70.dp), color = BorderGray, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction, categoryName: String, balancesHidden: Boolean) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isIncome = transaction.isIncome
        val bgColor  = if (isIncome) IncomeGreen else ExpenseRed
        val emoji    = if (isIncome) transaction.incomeType?.emoji else null
        val initial  = emoji ?: categoryName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

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
            val subtitle = if (isIncome && transaction.issuerName != null) {
                transaction.issuerName
            } else {
                formatDate(transaction.date)
            }
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        val prefix      = if (isIncome) "+" else "−"
        val amountColor = if (isIncome) IncomeGreen else ExpenseRed
        Text(
            text       = "$prefix ${maskAmount(formatAmount(transaction.amount), balancesHidden)} €",
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color      = amountColor
        )
    }
}

/** Resuelve el nombre a mostrar: para ingresos usa incomeType.label, para gastos usa categoryName. */
private fun resolveTransactionLabel(transaction: Transaction, categoryNames: Map<String, String>): String {
    return if (transaction.isIncome) {
        transaction.incomeType?.label ?: "Ingreso"
    } else {
        transaction.categoryId?.let { categoryNames[it] } ?: "Gasto"
    }
}

@Composable
private fun QuickAccessSection(onNavigateToCharts: () -> Unit = {}, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Acceso rápido", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAccessCard(label = "Gráficos", icon = "📊", onClick = onNavigateToCharts, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickAccessCard(label: String, icon: String, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Card(
        onClick   = onClick,
        modifier  = modifier.aspectRatio(1f),
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
