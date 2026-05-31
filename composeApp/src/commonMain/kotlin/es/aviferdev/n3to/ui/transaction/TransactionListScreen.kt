package es.aviferdev.n3to.ui.transaction

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.animation.AnimatedVisibility
import es.aviferdev.n3to.domain.model.IncomeTaxDetails
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.MonthlyTotals
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionLink
import es.aviferdev.n3to.domain.model.TransactionLinkType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.ui.common.navigation.TimeStepperHeader
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.home.bottomsheet.AddTransactionBottomSheet

import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.localizedMonthNames
import es.aviferdev.n3to.ui.common.input.SearchBar
import es.aviferdev.n3to.ui.transaction.components.*
import kotlinx.coroutines.delay
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.transaction_delete_message
import n3to.composeapp.generated.resources.transaction_delete_title
import n3to.composeapp.generated.resources.transaction_no_movements
import n3to.composeapp.generated.resources.transaction_no_results
import n3to.composeapp.generated.resources.transaction_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

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
            title = stringResource(Res.string.transaction_delete_title),
            message = stringResource(Res.string.transaction_delete_message),
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
        LaunchedEffect(tx.id) { viewModel.loadForEdit(tx) }
        AddTransactionBottomSheet(
            onDismiss = { viewModel.resetForCreate(); txToEdit = null },
            viewModel = viewModel
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
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        val monthNames = localizedMonthNames().map { it.replaceFirstChar { c -> c.uppercase() } }

        TopBarWithActionsApp(
            title = stringResource(Res.string.transaction_title),
            navigateBack = onBack
        )

        TimeStepperHeader(
            currentValue = monthNames.getOrElse(
                uiState.month.toIntOrNull()?.minus(1) ?: 0
            ) { uiState.month },
            currentValueSecondary = uiState.year,
            canGoBack = uiState.canGoBack,
            canGoForward = uiState.canGoForward,
            onPrevious = onPreviousMonth,
            onNext = onNextMonth,
            containerColor = MaterialTheme.appColors.navySurface,
            dividerColor = MaterialTheme.appColors.navyBorder,
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
        ) {
            Column {
                SearchBar(
                    query = searchQuery,
                    onChange = onSearchQueryChange,
                    backgroundColor = MaterialTheme.appColors.navySurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

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
            ) { CircularProgressIndicator(color = MaterialTheme.appColors.primary) }

            uiState.filteredTransactions.isEmpty() -> {
                val lowercaseMonthNames = localizedMonthNames()
                val monthName = uiState.month.toIntOrNull()?.let { mn ->
                    lowercaseMonthNames.getOrNull(mn - 1) ?: uiState.month
                } ?: uiState.month
                EmptyStateView(
                    icon = Icons.Outlined.Search,
                    title = if (searchQuery.isNotBlank()) "Sin resultados" else stringResource(Res.string.transaction_no_movements),
                    subtitle = if (searchQuery.isNotBlank()) stringResource(Res.string.transaction_no_results)
                    else "No hay movimientos en $monthName ${uiState.year}"
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                itemsIndexed(
                    items = uiState.filteredTransactions,
                    key = { _, t -> t.id }
                ) { index, transaction ->
                    val isLinkedToPropertyTx = transaction.linkedPropertyId != null
                    // Linked-to-asset or linked-to-property: no swipe, no edit
                    if (transaction.isLinkedToAsset || isLinkedToPropertyTx) {
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
                            color = MaterialTheme.appColors.navyBorder,
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
    val now = nowMillis()
    val fakeTransactions = listOf(
        Transaction(
            id = "tx_1", accountId = "acc_1", amount = 1500.0,
            type = TransactionType.INCOME, categoryId = null,
            date = now, notes = "Nómina", createdAt = now,
            taxDetails = IncomeTaxDetails(
                transactionId = "tx_1",
                incomeType = IncomeType.SALARY,
                grossAmount = 2000.0
            )
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
            taxDetails = IncomeTaxDetails(
                transactionId = "tx_3",
                incomeType = IncomeType.DIVIDEND
            ),
            links = listOf(
                TransactionLink(
                    id = "link_1",
                    linkType = TransactionLinkType.ASSET_TRANSACTION,
                    linkedEntityId = "linked_1"
                )
            )
        )
    )

    val fakeState = TransactionListUiState(
        transactions = fakeTransactions,
        filteredTransactions = fakeTransactions,
        totals = MonthlyTotals(
            "2024", "03", 1500.0, 45.50
        ),
        categoryNames = mapOf("cat_food" to "Alimentación"),
        year = "2024",
        month = "03",
        isLoading = false,
        canGoBack = true
    )

    N3toTheme {
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
            onTransactionClick = {},
            onBack = {}
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════

// EmptyState y DeleteConfirmDialog reemplazados por versiones de ui.common
