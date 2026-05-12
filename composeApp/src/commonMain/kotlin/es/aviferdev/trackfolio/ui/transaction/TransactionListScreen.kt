package es.aviferdev.trackfolio.ui.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.common.TrackfolioLabel
import es.aviferdev.trackfolio.ui.common.navigation.TimeStepperHeader
import es.aviferdev.trackfolio.ui.home.AddTransactionBottomSheet
import es.aviferdev.trackfolio.ui.home.AddTransactionViewModel
import es.aviferdev.trackfolio.ui.theme.BackgroundGray
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.IncomeGreen
import es.aviferdev.trackfolio.ui.theme.LocalBalanceHidden
import es.aviferdev.trackfolio.ui.theme.PrimaryAlpha
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import es.aviferdev.trackfolio.ui.theme.formatAmount
import es.aviferdev.trackfolio.ui.theme.formatDate
import es.aviferdev.trackfolio.ui.theme.maskAmount
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

// ═══════════════════════════════════════════════════════════════════════════════
// WRAPPER
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun TransactionListScreen(
    onBack: (() -> Unit)? = null,
    editTransactionId: String? = null,
    onConsumeEdit: () -> Unit = {},
    onTransactionClick: ((Transaction) -> Unit)? = null,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val balancesHidden = LocalBalanceHidden.current
    var txToDelete by remember { mutableStateOf<Transaction?>(null) }
    var txToEdit by remember { mutableStateOf<Transaction?>(null) }
    val addViewModel: AddTransactionViewModel = koinViewModel()
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    TransactionListContent(
        uiState = uiState,
        searchQuery = searchQuery,
        balancesHidden = balancesHidden,
        contentVisible = contentVisible,
        onBack = onBack,
        onPreviousMonth = { viewModel.previousMonth() },
        onNextMonth = { viewModel.nextMonth() },
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onDeleteTransaction = { txToDelete = it },
        onEditTransaction = { txToEdit = it },
        onTransactionClick = onTransactionClick
    )

    // ── Dialogs / Sheets ──────────────────────────────────────────────────────
    txToDelete?.let { tx ->
        DeleteConfirmDialog(
            onConfirm = { viewModel.deleteTransaction(tx.id); txToDelete = null },
            onDismiss = { txToDelete = null }
        )
    }

    // Recibir edición desde TransactionDetailScreen
    LaunchedEffect(editTransactionId) {
        if (editTransactionId != null) {
            val tx = uiState.transactions.find { it.id == editTransactionId }
            if (tx != null) {
                txToEdit = tx
            }
            onConsumeEdit()
        }
    }

    txToEdit?.let { tx ->
        LaunchedEffect(tx.id) { addViewModel.loadForEdit(tx) }
        AddTransactionBottomSheet(
            onDismiss = { addViewModel.resetForCreate(); txToEdit = null },
            viewModel = addViewModel
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun TransactionListContent(
    uiState: TransactionListUiState,
    searchQuery: String,
    balancesHidden: Boolean,
    contentVisible: Boolean,
    onBack: (() -> Unit)? = null,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onTransactionClick: ((Transaction) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        val monthName =
            MONTH_NAMES.getOrElse(uiState.month.toIntOrNull()?.minus(1) ?: 0) { uiState.month }
        TimeStepperHeader(
            title = "Movimientos",
            currentValue = monthName,
            currentValueSecondary = uiState.year,
            canGoBack = uiState.canGoBack,
            onPrevious = onPreviousMonth,
            onNext = onNextMonth,
            navigateBack = onBack
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
        ) {
            Column {
                SearchBar(query = searchQuery, onChange = onSearchQueryChange)

                uiState.totals?.let { totals ->
                    TotalsRow(
                        totalIncome = totals.totalIncome,
                        totalExpense = totals.totalExpense,
                        balancesHidden = balancesHidden
                    )
                }
            }
        }

        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = PrimaryDark) }

            uiState.filteredTransactions.isEmpty() -> EmptyState(
                month = uiState.month,
                year = uiState.year,
                isSearch = searchQuery.isNotBlank()
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                itemsIndexed(
                    items = uiState.filteredTransactions,
                    key = { _, t -> t.id }
                ) { index, transaction ->
                    // Linked-to-asset: no swipe, no edit
                    if (transaction.isLinkedToAsset) {
                        TransactionCard(
                            transaction = transaction,
                            label = TransactionViewModel.resolveLabel(
                                transaction,
                                uiState.categoryNames
                            ),
                            balancesHidden = balancesHidden,
                            onEdit = null,
                            onClick = { onTransactionClick?.invoke(transaction) }
                        )
                    } else {
                        SwipeToDeleteContainer(onDelete = { onDeleteTransaction(transaction) }) {
                            TransactionCard(
                                transaction = transaction,
                                label = TransactionViewModel.resolveLabel(
                                    transaction,
                                    uiState.categoryNames
                                ),
                                balancesHidden = balancesHidden,
                                onEdit = { onEditTransaction(transaction) },
                                onClick = { onTransactionClick?.invoke(transaction) }
                            )
                        }
                    }
                    if (index < uiState.filteredTransactions.lastIndex) {
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

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════════════════════════════════════════════

@Preview
@Composable
fun TransactionListContentPreview() {
    val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    val fakeTransactions = listOf(
        Transaction(
            id = "tx_1", accountId = "acc_1", amount = 1500.0,
            type = TransactionType.INCOME, categoryId = null,
            date = now, notes = "Nómina", createdAt = now,
            incomeType = es.aviferdev.trackfolio.domain.model.IncomeType.SALARY,
            grossAmount = 2000.0, irpfPercent = 19.0
        ),
        Transaction(
            id = "tx_2", accountId = "acc_1", amount = -45.50,
            type = TransactionType.EXPENSE, categoryId = "cat_food",
            date = now, notes = "Supermercado", createdAt = now
        ),
        Transaction(
            id = "tx_3", accountId = "acc_1", amount = 25.0,
            type = TransactionType.INCOME, categoryId = null,
            date = now, notes = "Dividendo AAPL", createdAt = now,
            incomeType = es.aviferdev.trackfolio.domain.model.IncomeType.DIVIDEND,
            linkedAssetTransactionId = "linked_1"
        )
    )

    val fakeState = TransactionListUiState(
        transactions = fakeTransactions,
        filteredTransactions = fakeTransactions,
        totals = es.aviferdev.trackfolio.domain.model.MonthlyTotals(
            "2024", "03", 1500.0, 45.50
        ),
        categoryNames = mapOf("cat_food" to "Alimentación"),
        year = "2024",
        month = "03",
        isLoading = false,
        canGoBack = true
    )

    TrackfolioTheme {
        TransactionListContent(
            uiState = fakeState,
            searchQuery = "",
            balancesHidden = false,
            contentVisible = true,
            onPreviousMonth = {},
            onNextMonth = {},
            onSearchQueryChange = {},
            onDeleteTransaction = {},
            onEditTransaction = {},
            onTransactionClick = {}
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Subcomponentes (sin cambios)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SearchBar(query: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(SurfaceWhite)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Buscar",
            tint = TextTertiary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 12.sp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        "Buscar por nota, categoría o emisor…",
                        fontSize = 12.sp,
                        color = TextTertiary
                    )
                }
                inner()
            }
        )
        if (query.isNotBlank()) {
            TextButton(
                onClick = { onChange("") },
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text("×", fontSize = 16.sp, color = TextTertiary)
            }
        }
    }
}

@Composable
private fun TotalsRow(totalIncome: Double, totalExpense: Double, balancesHidden: Boolean) {
    val balance = totalIncome - totalExpense
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TotalCell(
                label = "Ingresos", amount = totalIncome, color = IncomeGreen,
                prefix = "+", balancesHidden = balancesHidden, modifier = Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(40.dp).background(BorderGray))
            TotalCell(
                label = "Gastos", amount = totalExpense, color = ExpenseRed,
                prefix = "−", balancesHidden = balancesHidden, modifier = Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(40.dp).background(BorderGray))
            TotalCell(
                label = "Balance", amount = kotlin.math.abs(balance),
                color = if (balance >= 0) PrimaryDark else ExpenseRed,
                prefix = if (balance >= 0) "+" else "−",
                balancesHidden = balancesHidden, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TotalCell(
    label: String, amount: Double, color: Color, prefix: String,
    balancesHidden: Boolean, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrackfolioLabel(text = label, modifier = Modifier.padding(bottom = 4.dp))
        Text(
            "$prefix ${maskAmount(formatAmount(amount), balancesHidden)} €",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    label: String,
    balancesHidden: Boolean,
    onEdit: (() -> Unit)?,
    onClick: (() -> Unit)? = null,
) {
    val isIncome = transaction.isIncome
    val isAdjustment = transaction.isAdjustment
    val isLinked = transaction.isLinkedToAsset
    val avatarBg = when {
        isAdjustment -> PrimaryDark; isLinked -> PrimaryDark
        isIncome -> IncomeGreen; else -> ExpenseRed
    }
    val avatarIcon = when {
        isAdjustment -> Icons.Outlined.SwapHoriz
        isLinked -> Icons.Outlined.ShowChart
        isIncome -> Icons.Outlined.ArrowDownward
        else -> Icons.Outlined.ArrowUpward
    }
    val avatarContentDesc = when {
        isAdjustment -> "Ajuste"
        isLinked -> "Inversión"
        isIncome -> "Ingreso"
        else -> "Gasto"
    }
    val prefix = when {
        isAdjustment && transaction.amount >= 0 -> "+"
        isAdjustment -> "−"; isIncome -> "+"; else -> "−"
    }
    val amountColor = when {
        isAdjustment -> PrimaryDark; isIncome -> IncomeGreen; else -> ExpenseRed
    }
    val displayAmount =
        if (isAdjustment) kotlin.math.abs(transaction.amount) else transaction.amount
    val subtitle = when {
        isIncome && transaction.issuerName != null -> transaction.issuerName!!
        !transaction.notes.isNullOrBlank() -> transaction.notes
        else -> null
    }
    val dateFormatted = formatDate(transaction.date)

    Row(
        modifier = Modifier.fillMaxWidth().background(SurfaceWhite)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar con icono Material
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(avatarBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = avatarIcon,
                contentDescription = avatarContentDesc,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            if (subtitle != null || dateFormatted.isNotEmpty()) {
                Spacer(Modifier.height(1.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            fontSize = 11.sp,
                            color    = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (subtitle != null && dateFormatted.isNotEmpty()) {
                        Spacer(Modifier.width(4.dp))
                        Text("·", fontSize = 11.sp, color = TextTertiary)
                        Spacer(Modifier.width(4.dp))
                    }
                    if (dateFormatted.isNotEmpty()) {
                        Text(
                            dateFormatted,
                            fontSize = 11.sp,
                            color    = TextTertiary,
                            maxLines = 1
                        )
                    }
                }
            }
            if (isIncome && transaction.incomeType != null && transaction.grossAmount != null) {
                Spacer(Modifier.height(2.dp))
                IncomeBadge(transaction)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "$prefix ${maskAmount(formatAmount(displayAmount), balancesHidden)} €",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = amountColor,
                maxLines   = 1
            )
            if (isIncome && transaction.grossAmount != null && !balancesHidden) {
                Text(
                    "Bruto: ${formatAmount(transaction.grossAmount)} €",
                    fontSize = 9.sp,
                    color    = TextTertiary,
                    maxLines = 1
                )
            }
            if (isLinked) {
                Text("Portfolio", fontSize = 9.sp, color = PrimaryDark.copy(alpha = 0.6f))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "›",
            fontSize = 20.sp,
            color = TextTertiary,
            fontWeight = FontWeight.Light,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

private fun incomeTypeIcon(incomeType: IncomeType): ImageVector = when (incomeType) {
    IncomeType.SALARY -> Icons.Outlined.Badge
    IncomeType.BANK_INTEREST -> Icons.Outlined.AccountBalance
    IncomeType.BOND_DEPOSIT -> Icons.Outlined.RequestQuote
    IncomeType.DIVIDEND -> Icons.Outlined.ShowChart
    IncomeType.BONUS_PRIZE -> Icons.Outlined.CardGiftcard
    IncomeType.EXEMPT_INCOME -> Icons.Outlined.CheckCircle
}

@Composable
private fun IncomeBadge(transaction: Transaction) {
    val incType = transaction.incomeType ?: return
    val pct = transaction.irpfPercent
    Row(
        modifier = Modifier
            .background(PrimaryAlpha, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = incomeTypeIcon(incType),
            contentDescription = incType.label,
            tint = PrimaryDark,
            modifier = Modifier.size(11.dp)
        )
        val text = buildString {
            append(incType.label)
            if (pct != null && pct > 0) append(" · ${pct.toLong()}% IRPF")
        }
        Text(text, fontSize = 8.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContainer(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { v ->
            if (v == SwipeToDismissBoxValue.EndToStart) {
                onDelete(); false
            } else false
        }
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val bg by animateColorAsState(
                targetValue = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    ExpenseRed else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier.fillMaxSize().background(bg).padding(end = 18.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    "Eliminar",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) { Surface(color = SurfaceWhite) { content() } }
}

@Composable
private fun EmptyState(month: String, year: String, isSearch: Boolean) {
    val monthName = MONTH_NAMES.getOrElse(month.toIntOrNull()?.minus(1) ?: 0) { month }
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (isSearch) "Sin resultados" else "Sin movimientos",
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                color = TextPrimary, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (isSearch) "No hay movimientos que coincidan con tu búsqueda"
                else "No hay movimientos en $monthName $year",
                fontSize = 13.sp, color = TextTertiary, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                "Eliminar movimiento",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                "¿Seguro que quieres eliminar este movimiento? Esta acción no se puede deshacer.",
                fontSize = 13.sp,
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Eliminar",
                    color = ExpenseRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancelar",
                    color = PrimaryDark,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
