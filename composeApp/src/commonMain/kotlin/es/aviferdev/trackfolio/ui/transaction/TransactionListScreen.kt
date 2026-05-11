package es.aviferdev.trackfolio.ui.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import es.aviferdev.trackfolio.ui.common.*
import es.aviferdev.trackfolio.ui.home.AddTransactionBottomSheet
import es.aviferdev.trackfolio.ui.home.AddTransactionViewModel
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

private val MONTH_NAMES = listOf(
    "Enero","Febrero","Marzo","Abril","Mayo","Junio",
    "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
)

@Composable
fun TransactionListScreen(viewModel: TransactionViewModel = koinViewModel()) {
    val uiState        by viewModel.uiState.collectAsState()
    val searchQuery    by viewModel.searchQuery.collectAsState()
    val balancesHidden = LocalBalanceHidden.current
    var txToDelete     by remember { mutableStateOf<Transaction?>(null) }
    var txToEdit       by remember { mutableStateOf<Transaction?>(null) }
    val addViewModel: AddTransactionViewModel = koinViewModel()

    // ── Staggered entrance animation ──────────────────────────────────────────
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        MonthHeader(
            year       = uiState.year,
            month      = uiState.month,
            canGoBack  = uiState.canGoBack,
            onPrevious = { viewModel.previousMonth() },
            onNext     = { viewModel.nextMonth() }
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
        ) {
            Column {
                SearchBar(query = searchQuery, onChange = { viewModel.onSearchQueryChange(it) })

                uiState.totals?.let { totals ->
                    TotalsRow(
                        totalIncome    = totals.totalIncome,
                        totalExpense   = totals.totalExpense,
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
                month    = uiState.month,
                year     = uiState.year,
                isSearch = searchQuery.isNotBlank()
            )

            else -> LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                itemsIndexed(
                    items = uiState.filteredTransactions,
                    key   = { _, t -> t.id }
                ) { index, transaction ->
                    // Linked-to-asset: no swipe, no edit
                    if (transaction.isLinkedToAsset) {
                        TransactionCard(
                            transaction    = transaction,
                            label          = TransactionViewModel.resolveLabel(transaction, uiState.categoryNames),
                            balancesHidden = balancesHidden,
                            onEdit         = null
                        )
                    } else {
                        SwipeToDeleteContainer(onDelete = { txToDelete = transaction }) {
                            TransactionCard(
                                transaction    = transaction,
                                label          = TransactionViewModel.resolveLabel(transaction, uiState.categoryNames),
                                balancesHidden = balancesHidden,
                                onEdit         = { txToEdit = transaction }
                            )
                        }
                    }
                    if (index < uiState.filteredTransactions.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(start = 68.dp),
                            color     = BorderGray,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }

    // ── Dialogs / Sheets ──────────────────────────────────────────────────────
    txToDelete?.let { tx ->
        DeleteConfirmDialog(
            onConfirm = { viewModel.deleteTransaction(tx.id); txToDelete = null },
            onDismiss = { txToDelete = null }
        )
    }

    txToEdit?.let { tx ->
        LaunchedEffect(tx.id) { addViewModel.loadForEdit(tx) }
        AddTransactionBottomSheet(
            onDismiss = { addViewModel.resetForCreate(); txToEdit = null },
            viewModel = addViewModel
        )
    }
}

// ─── Month header ─────────────────────────────────────────────────────────────
@Composable
private fun MonthHeader(
    year: String,
    month: String,
    canGoBack: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val monthName = MONTH_NAMES.getOrElse(month.toIntOrNull()?.minus(1) ?: 0) { month }

    Surface(color = SurfaceWhite, shadowElevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 14.dp)
        ) {
            Text(
                "Movimientos",
                fontSize      = 18.sp,
                fontWeight    = FontWeight.Bold,
                color         = TextPrimary,
                letterSpacing = (-0.3).sp
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                MonthNavButton(label = "‹", enabled = canGoBack, onClick = onPrevious)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(monthName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(year, fontSize = 11.sp, color = TextTertiary)
                }
                MonthNavButton(label = "›", enabled = month.toIntOrNull() ?: 12 < 12, onClick = onNext)
            }
        }
    }
    HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
}

@Composable
private fun MonthNavButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (enabled) SurfaceElevated else Color.Transparent)
    ) {
        Text(label, fontSize = 22.sp, color = if (enabled) TextPrimary else TextTertiary, fontWeight = FontWeight.Light)
    }
}

// ─── Search bar ──────────────────────────────────────────────────────────────
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
        Text("🔍", fontSize = 15.sp)
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value           = query,
            onValueChange   = onChange,
            singleLine      = true,
            modifier        = Modifier.weight(1f),
            textStyle       = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 12.sp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("Buscar por nota, categoría o emisor…", fontSize = 12.sp, color = TextTertiary)
                }
                inner()
            }
        )
        if (query.isNotBlank()) {
            TextButton(onClick = { onChange("") }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text("×", fontSize = 16.sp, color = TextTertiary)
            }
        }
    }
}

// ─── Totals row (3 columnas estilo JSX) ───────────────────────────────────────
@Composable
private fun TotalsRow(
    totalIncome: Double,
    totalExpense: Double,
    balancesHidden: Boolean
) {
    val balance = totalIncome - totalExpense

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TotalCell(
                label          = "Ingresos",
                amount         = totalIncome,
                color          = IncomeGreen,
                prefix         = "+",
                balancesHidden = balancesHidden,
                modifier       = Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(40.dp).background(BorderGray))
            TotalCell(
                label          = "Gastos",
                amount         = totalExpense,
                color          = ExpenseRed,
                prefix         = "−",
                balancesHidden = balancesHidden,
                modifier       = Modifier.weight(1f)
            )
            Box(Modifier.width(1.dp).height(40.dp).background(BorderGray))
            TotalCell(
                label          = "Balance",
                amount         = kotlin.math.abs(balance),
                color          = if (balance >= 0) PrimaryDark else ExpenseRed,
                prefix         = if (balance >= 0) "+" else "−",
                balancesHidden = balancesHidden,
                modifier       = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TotalCell(
    label: String,
    amount: Double,
    color: Color,
    prefix: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TrackfolioLabel(text = label, modifier = Modifier.padding(bottom = 4.dp))
        Text(
            "$prefix ${maskAmount(formatAmount(amount), balancesHidden)} €",
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            color      = color,
            textAlign  = TextAlign.Center
        )
    }
}

// ─── Transaction card ─────────────────────────────────────────────────────────
@Composable
private fun TransactionCard(
    transaction: Transaction,
    label: String,
    balancesHidden: Boolean,
    onEdit: (() -> Unit)?
) {
    val isIncome     = transaction.isIncome
    val isAdjustment = transaction.isAdjustment
    val isLinked     = transaction.isLinkedToAsset

    val avatarBg = when {
        isAdjustment -> PrimaryDark
        isLinked     -> PrimaryDark
        isIncome     -> IncomeGreen
        else         -> ExpenseRed
    }
    val initial = when {
        isAdjustment -> "⚖"
        isLinked     -> "📈"
        isIncome     -> transaction.incomeType?.emoji ?: label.firstOrNull()?.uppercase() ?: "?"
        else         -> label.firstOrNull()?.uppercase() ?: "?"
    }

    val prefix = when {
        isAdjustment && transaction.amount >= 0 -> "+"
        isAdjustment -> "−"
        isIncome     -> "+"
        else         -> "−"
    }
    val amountColor = when {
        isAdjustment -> PrimaryDark
        isIncome     -> IncomeGreen
        else         -> ExpenseRed
    }
    val displayAmount = if (isAdjustment) kotlin.math.abs(transaction.amount) else transaction.amount

    val subtitle = when {
        isIncome && transaction.issuerName != null -> transaction.issuerName!!
        !transaction.notes.isNullOrBlank()         -> transaction.notes!!
        else                                       -> formatDate(transaction.date)
    }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar usando componente común
        InitialsAvatar(
            text     = initial,
            bgColor  = avatarBg,
            size     = 40.dp,
            textSize = 15
        )

        Spacer(Modifier.width(12.dp))

        // Label + subtitle + badge fiscal
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = TextTertiary)
            if (isIncome && transaction.incomeType != null && transaction.grossAmount != null) {
                Spacer(Modifier.height(4.dp))
                IncomeBadge(transaction)
            }
        }

        // Importe + acciones
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$prefix ${maskAmount(formatAmount(displayAmount), balancesHidden)} €",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = amountColor
            )
            if (isIncome && transaction.grossAmount != null && !balancesHidden) {
                Text("Bruto: ${formatAmount(transaction.grossAmount)} €", fontSize = 9.sp, color = TextTertiary)
            }
            Text(formatDate(transaction.date), fontSize = 10.sp, color = TextTertiary)
            when {
                onEdit != null -> TextButton(
                    onClick        = onEdit,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier       = Modifier.height(18.dp)
                ) {
                    Text("Editar", fontSize = 9.sp, color = PrimaryDark)
                }
                isLinked -> Text("Portfolio", fontSize = 9.sp, color = PrimaryDark.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun IncomeBadge(transaction: Transaction) {
    val incType = transaction.incomeType ?: return
    val pct     = transaction.irpfPercent

    Row(
        modifier = Modifier
            .background(PrimaryAlpha, RoundedCornerShape(5.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(incType.emoji, fontSize = 9.sp)
        val text = buildString {
            append(incType.label)
            if (pct != null && pct > 0) append(" · ${pct.toLong()}% IRPF")
        }
        Text(text, fontSize = 9.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
    }
}

// ─── Swipe to delete (PRESERVAR - funcionalidad existente) ──────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContainer(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { v ->
            if (v == SwipeToDismissBoxValue.EndToStart) { onDelete(); false } else false
        }
    )
    SwipeToDismissBox(
        state                       = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val bg by animateColorAsState(
                targetValue = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    ExpenseRed else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(end = 18.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Eliminar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    ) {
        Surface(color = SurfaceWhite) { content() }
    }
}

// ─── Empty state ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(month: String, year: String, isSearch: Boolean) {
    val monthName = MONTH_NAMES.getOrElse(month.toIntOrNull()?.minus(1) ?: 0) { month }
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (isSearch) "Sin resultados" else "Sin movimientos",
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (isSearch) "No hay movimientos que coincidan con tu búsqueda"
                else "No hay movimientos en $monthName $year",
                fontSize  = 13.sp,
                color     = TextTertiary,
                textAlign = TextAlign.Center
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
        title = {
            Text("Eliminar movimiento", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Text("¿Seguro que quieres eliminar este movimiento? Esta acción no se puede deshacer.", fontSize = 13.sp, color = TextSecondary)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}