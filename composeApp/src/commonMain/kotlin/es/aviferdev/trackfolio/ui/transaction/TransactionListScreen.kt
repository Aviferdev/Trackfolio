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
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        MonthHeader(
            year = uiState.year,
            month = uiState.month,
            onPrevious = { viewModel.previousMonth() },
            onNext = { viewModel.nextMonth() }
        )

        uiState.totals?.let { totals ->
            TotalsCard(
                totalIncome = totals.totalIncome,
                totalExpense = totals.totalExpense
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryDark)
            }
        } else if (uiState.transactions.isEmpty()) {
            EmptyState(month = uiState.month, year = uiState.year)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                itemsIndexed(
                    items = uiState.transactions,
                    key = { _, t -> t.id }
                ) { index, transaction ->
                    SwipeToDeleteContainer(
                        onDelete = { transactionToDelete = transaction }
                    ) {
                        TransactionListRow(transaction = transaction)
                    }
                    if (index < uiState.transactions.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = BorderGray,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }

    transactionToDelete?.let { transaction ->
        DeleteConfirmDialog(
            onConfirm = {
                viewModel.deleteTransaction(transaction.id)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }
}

@Composable
private fun MonthHeader(
    year: String,
    month: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val isCurrentMonth = year == now.year.toString() &&
        month == now.monthNumber.toString().padStart(2, '0')

    val monthName = MONTH_NAMES.getOrElse(month.toIntOrNull()?.minus(1) ?: 0) { month }

    Surface(
        color = SurfaceWhite,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Movimientos",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BackgroundGray)
                ) {
                    Text("‹", fontSize = 22.sp, color = TextPrimary, fontWeight = FontWeight.Light)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = year,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onNext,
                    enabled = !isCurrentMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (!isCurrentMonth) BackgroundGray else Color.Transparent)
                ) {
                    Text(
                        "›",
                        fontSize = 22.sp,
                        color = if (!isCurrentMonth) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalsCard(
    totalIncome: Double,
    totalExpense: Double
) {
    val balance = totalIncome - totalExpense

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            TotalItem(
                label = "Ingresos",
                amount = totalIncome,
                color = IncomeGreen,
                prefix = "+",
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(44.dp)
                    .background(BorderGray)
                    .align(Alignment.CenterVertically)
            )

            TotalItem(
                label = "Gastos",
                amount = totalExpense,
                color = ExpenseRed,
                prefix = "−",
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(44.dp)
                    .background(BorderGray)
                    .align(Alignment.CenterVertically)
            )

            TotalItem(
                label = "Balance",
                amount = balance,
                color = if (balance >= 0) IncomeGreen else ExpenseRed,
                prefix = if (balance >= 0) "+" else "−",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TotalItem(
    label: String,
    amount: Double,
    color: Color,
    prefix: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$prefix ${formatAmount(kotlin.math.abs(amount))} €",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Eliminar",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) {
        Surface(color = SurfaceWhite) {
            content()
        }
    }
}

@Composable
private fun TransactionListRow(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isIncome = transaction.type == TransactionType.INCOME
        val bgColor = if (isIncome) IncomeGreen else ExpenseRed
        val initial = transaction.categoryId.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.categoryId,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            if (!transaction.notes.isNullOrBlank()) {
                Text(
                    text = transaction.notes,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = formatDate(transaction.date),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val prefix = if (isIncome) "+" else "−"
            val amountColor = if (isIncome) IncomeGreen else ExpenseRed
            Text(
                text = "$prefix ${formatAmount(transaction.amount)} €",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
            Text(
                text = formatDate(transaction.date),
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyState(month: String, year: String) {
    val monthName = MONTH_NAMES.getOrElse(month.toIntOrNull()?.minus(1) ?: 0) { month }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Sin movimientos",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No hay movimientos registrados en $monthName $year",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = "Eliminar movimiento",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "¿Seguro que quieres eliminar este movimiento? Esta acción no se puede deshacer.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Eliminar",
                    color = ExpenseRed,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = PrimaryDark,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

private fun formatAmount(amount: Double): String {
    val rounded = (amount * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val eurosStr = buildString {
        euros.toString().reversed().forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append('.')
            append(c)
        }
    }.reversed()
    return "$eurosStr,${cents.toString().padStart(2, '0')}"
}

private fun formatDate(epochMillis: Long): String {
    val days = epochMillis / 86_400_000L
    val today = Clock.System.now().toEpochMilliseconds() / 86_400_000L
    return when (days) {
        today -> "hoy"
        today - 1 -> "ayer"
        else -> {
            val totalDays = epochMillis / 86_400_000L
            val y = 1970 + (totalDays / 365).toInt()
            val dayOfYear = (totalDays % 365).toInt()
            val m = (dayOfYear / 30) + 1
            val d = (dayOfYear % 30) + 1
            "${d.toString().padStart(2, '0')}/${m.toString().padStart(2, '0')}/$y"
        }
    }
}
