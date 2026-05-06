package es.aviferdev.trackfolio.ui.transaction

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import es.aviferdev.trackfolio.ui.home.AddTransactionBottomSheet
import es.aviferdev.trackfolio.ui.home.AddTransactionViewModel
import org.koin.compose.viewmodel.koinViewModel

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

@Composable
fun TransactionListScreen(
    viewModel: TransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val balancesHidden = LocalBalanceHidden.current
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var transactionToEdit   by remember { mutableStateOf<Transaction?>(null) }
    val addViewModel: AddTransactionViewModel = koinViewModel()

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        MonthHeader(
            year      = uiState.year,
            month     = uiState.month,
            onPrevious = { viewModel.previousMonth() },
            onNext     = { viewModel.nextMonth() }
        )
        SearchBar(query = searchQuery, onChange = { viewModel.onSearchQueryChange(it) })

        uiState.totals?.let { totals ->
            TotalsCard(
                totalIncome    = totals.totalIncome,
                totalExpense   = totals.totalExpense,
                balancesHidden = balancesHidden
            )
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryDark)
            }
        } else if (uiState.filteredTransactions.isEmpty()) {
            EmptyState(month = uiState.month, year = uiState.year, isSearch = searchQuery.isNotBlank())
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                itemsIndexed(
                    items = uiState.filteredTransactions,
                    key   = { _, t -> t.id }
                ) { index, transaction ->
                    SwipeToDeleteContainer(onDelete = { transactionToDelete = transaction }) {
                        TransactionListRow(
                            transaction    = transaction,
                            categoryName   = uiState.categoryNames[transaction.categoryId] ?: transaction.categoryId,
                            balancesHidden = balancesHidden,
                            onEdit         = { transactionToEdit = transaction }
                        )
                    }
                    if (index < uiState.filteredTransactions.lastIndex) {
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

    transactionToDelete?.let { transaction ->
        DeleteConfirmDialog(
            onConfirm = { viewModel.deleteTransaction(transaction.id); transactionToDelete = null },
            onDismiss = { transactionToDelete = null }
        )
    }

    transactionToEdit?.let { transaction ->
        LaunchedEffect(transaction.id) { addViewModel.loadForEdit(transaction) }
        AddTransactionBottomSheet(
            onDismiss = { addViewModel.resetForCreate(); transactionToEdit = null },
            viewModel = addViewModel
        )
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────
@Composable
private fun SearchBar(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value         = query,
        onValueChange = onChange,
        placeholder   = { Text("Buscar por nota o categoría…", fontSize = 14.sp, color = TextSecondary.copy(alpha = 0.6f)) },
        modifier      = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape         = RoundedCornerShape(10.dp),
        singleLine    = true,
        colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray),
        trailingIcon  = if (query.isNotBlank()) {{
            TextButton(onClick = { onChange("") }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("×", fontSize = 18.sp, color = TextSecondary)
            }
        }} else null
    )
}

// ─── Month header ─────────────────────────────────────────────────────────────
@Composable
private fun MonthHeader(year: String, month: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    val now           = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val isCurrentMonth = year == now.year.toString() && month == now.monthNumber.toString().padStart(2, '0')
    val monthName     = MONTH_NAMES.getOrElse(month.toIntOrNull()?.minus(1) ?: 0) { month }

    Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text("Movimientos", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp).clip(CircleShape).background(BackgroundGray)) {
                    Text("‹", fontSize = 22.sp, color = TextPrimary, fontWeight = FontWeight.Light)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(monthName, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(year, fontSize = 13.sp, color = TextSecondary)
                }
                IconButton(
                    onClick  = onNext,
                    enabled  = !isCurrentMonth,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(if (!isCurrentMonth) BackgroundGray else Color.Transparent)
                ) {
                    Text("›", fontSize = 22.sp, color = if (!isCurrentMonth) TextPrimary else TextSecondary.copy(alpha = 0.3f), fontWeight = FontWeight.Light)
                }
            }
        }
    }
}

// ─── Totals card ──────────────────────────────────────────────────────────────
@Composable
private fun TotalsCard(totalIncome: Double, totalExpense: Double, balancesHidden: Boolean) {
    val balance = totalIncome - totalExpense
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
            TotalItem(label = "Ingresos", amount = totalIncome, color = IncomeGreen, prefix = "+", balancesHidden = balancesHidden, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.width(0.5.dp).height(44.dp).background(BorderGray).align(Alignment.CenterVertically))
            TotalItem(label = "Gastos",   amount = totalExpense, color = ExpenseRed,  prefix = "−", balancesHidden = balancesHidden, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.width(0.5.dp).height(44.dp).background(BorderGray).align(Alignment.CenterVertically))
            TotalItem(label = "Balance",  amount = balance, color = if (balance >= 0) IncomeGreen else ExpenseRed, prefix = if (balance >= 0) "+" else "−", balancesHidden = balancesHidden, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TotalItem(label: String, amount: Double, color: Color, prefix: String, balancesHidden: Boolean, modifier: Modifier = Modifier) {
    val abs = if (amount < 0) -amount else amount
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            "$prefix ${maskAmount(formatAmount(abs), balancesHidden)} €",
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = color,
            textAlign  = TextAlign.Center
        )
    }
}

// ─── Swipe to delete ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContainer(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); false } else false
        }
    )
    SwipeToDismissBox(
        state                       = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> ExpenseRed
                    else -> Color.Transparent
                },
                label = "swipe_bg"
            )
            Box(modifier = Modifier.fillMaxSize().background(color).padding(end = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Text("Eliminar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    ) {
        Surface(color = SurfaceWhite) { content() }
    }
}

// ─── Transaction row ──────────────────────────────────────────────────────────
@Composable
private fun TransactionListRow(
    transaction: Transaction,
    categoryName: String,
    balancesHidden: Boolean,
    onEdit: () -> Unit
) {
    val isIncome   = transaction.type == TransactionType.INCOME
    val bgColor    = if (isIncome) IncomeGreen else ExpenseRed
    val initial    = categoryName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val hasFiscal  = isIncome && transaction.taxType != null

    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier.size(42.dp).clip(CircleShape).background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(initial, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(categoryName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            // Nota o fecha
            Text(
                text     = if (!transaction.notes.isNullOrBlank()) transaction.notes else formatDate(transaction.date),
                fontSize = 12.sp,
                color    = TextSecondary
            )
            // Badge fiscal — solo si hay datos IRPF
            if (hasFiscal) {
                Spacer(Modifier.height(3.dp))
                FiscalBadge(transaction)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val prefix      = if (isIncome) "+" else "−"
            val amountColor = if (isIncome) IncomeGreen else ExpenseRed
            // Importe neto (el que entra en la cuenta)
            Text(
                "$prefix ${maskAmount(formatAmount(transaction.amount), balancesHidden)} €",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = amountColor
            )
            // Bruto si es diferente del neto
            if (hasFiscal && transaction.grossAmount != null && !balancesHidden) {
                Text(
                    "Bruto: ${formatAmount(transaction.grossAmount)} €",
                    fontSize = 10.sp,
                    color    = TextSecondary
                )
            }
            Text(formatDate(transaction.date), fontSize = 11.sp, color = TextSecondary)
            TextButton(
                onClick        = onEdit,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                modifier       = Modifier.height(20.dp)
            ) {
                Text("Editar", fontSize = 10.sp, color = PrimaryDark.copy(alpha = 0.7f))
            }
        }
    }
}

/** Pastilla informativa con el tipo IRPF y el porcentaje de retención. */
@Composable
private fun FiscalBadge(transaction: Transaction) {
    val taxType = transaction.taxType ?: return
    val pct     = transaction.irpfPercent

    Row(
        modifier          = Modifier
            .background(PrimaryDark.copy(alpha = 0.07f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(taxType.emoji, fontSize = 10.sp)
        Text(
            text     = if (pct != null) "${taxType.label} · ${pct.toLong()}% IRPF"
                       else taxType.label,
            fontSize = 9.sp,
            color    = PrimaryDark,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(month: String, year: String, isSearch: Boolean = false) {
    val monthName = MONTH_NAMES.getOrElse(month.toIntOrNull()?.minus(1) ?: 0) { month }
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (isSearch) "Sin resultados" else "Sin movimientos", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = TextPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                if (isSearch) "No hay movimientos que coincidan con tu búsqueda"
                else "No hay movimientos en $monthName $year",
                fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Delete dialog ────────────────────────────────────────────────────────────
@Composable
private fun DeleteConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceWhite,
        title            = { Text("Eliminar movimiento", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
        text             = { Text("¿Seguro que quieres eliminar este movimiento? Esta acción no se puede deshacer.", fontSize = 14.sp, color = TextSecondary) },
        confirmButton    = { TextButton(onClick = onConfirm) { Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.Medium) } },
        dismissButton    = { TextButton(onClick = onDismiss) { Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium) } },
        shape            = RoundedCornerShape(16.dp)
    )
}
