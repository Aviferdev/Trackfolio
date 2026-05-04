package es.aviferdev.trackfolio.ui.debt

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DebtListScreen(
    viewModel: DebtViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDebt by remember { mutableStateOf(false) }
    var debtToMarkPaid by remember { mutableStateOf<Debt?>(null) }
    var debtToDelete by remember { mutableStateOf<Debt?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = PrimaryDark
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    DebtHeader(
                        totalTheyOwe = uiState.totalTheyOwe,
                        totalIOwe = uiState.totalIOwe
                    )
                }

                if (uiState.debtsTheyOwe.isNotEmpty()) {
                    item {
                        DebtSectionTitle(
                            title = "Me deben",
                            total = uiState.totalTheyOwe,
                            color = IncomeGreen
                        )
                    }
                    items(uiState.debtsTheyOwe, key = { it.id }) { debt ->
                        SwipeToDeleteDebtContainer(
                            onDelete = { debtToDelete = debt }
                        ) {
                            DebtCard(
                                debt = debt,
                                onMarkPaid = { debtToMarkPaid = debt }
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = BorderGray,
                            thickness = 0.5.dp
                        )
                    }
                }

                if (uiState.debtsIOwe.isNotEmpty()) {
                    item {
                        DebtSectionTitle(
                            title = "Debo yo",
                            total = uiState.totalIOwe,
                            color = ExpenseRed
                        )
                    }
                    items(uiState.debtsIOwe, key = { it.id }) { debt ->
                        SwipeToDeleteDebtContainer(
                            onDelete = { debtToDelete = debt }
                        ) {
                            DebtCard(
                                debt = debt,
                                onMarkPaid = { debtToMarkPaid = debt }
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = BorderGray,
                            thickness = 0.5.dp
                        )
                    }
                }

                if (uiState.debtsTheyOwe.isEmpty() && uiState.debtsIOwe.isEmpty()) {
                    item { DebtEmptyState() }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDebt = true },
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

    if (showAddDebt) {
        AddDebtBottomSheet(
            onDismiss = { showAddDebt = false },
            viewModel = viewModel
        )
    }

    debtToDelete?.let { debt ->
        DeleteDebtDialog(
            personName = debt.personName,
            onConfirm = {
                viewModel.deleteDebt(debt.id)
                debtToDelete = null
            },
            onDismiss = { debtToDelete = null }
        )
    }

    debtToMarkPaid?.let { debt ->
        MarkPaidDialog(
            personName = debt.personName,
            amount = debt.amount,
            onConfirm = {
                viewModel.markAsPaid(debt.id)
                debtToMarkPaid = null
            },
            onDismiss = { debtToMarkPaid = null }
        )
    }
}

@Composable
private fun DebtHeader(
    totalTheyOwe: Double,
    totalIOwe: Double
) {
    Surface(color = SurfaceWhite, shadowElevation = 1.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 20.dp)
        ) {
            Text(
                text = "Deudas",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryDark),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    DebtSummaryItem(
                        label = "Me deben",
                        amount = totalTheyOwe,
                        color = Color(0xFF66BB6A),
                        modifier = Modifier.weight(1f),
                        align = Alignment.Start
                    )
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(48.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                            .align(Alignment.CenterVertically)
                    )
                    DebtSummaryItem(
                        label = "Debo yo",
                        amount = totalIOwe,
                        color = Color(0xFFEF9A9A),
                        modifier = Modifier.weight(1f),
                        align = Alignment.End
                    )
                }
            }
        }
    }
}

@Composable
private fun DebtSummaryItem(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier,
    align: Alignment.Horizontal
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = align
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f))
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${formatAmount(amount)} €",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun DebtSectionTitle(
    title: String,
    total: Double,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        Text(
            text = "${formatAmount(total)} €",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun DebtCard(
    debt: Debt,
    onMarkPaid: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isTheyOwe = debt.direction == DebtDirection.THEY_OWE
        val avatarColor = if (isTheyOwe) IncomeGreen else ExpenseRed
        val initial = debt.personName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(avatarColor),
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
                text = debt.personName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (!debt.notes.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = debt.notes,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val amountColor = if (isTheyOwe) IncomeGreen else ExpenseRed
            val prefix = if (isTheyOwe) "+" else "−"
            Text(
                text = "$prefix ${formatAmount(debt.amount)} €",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = onMarkPaid,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Text(
                    text = "Marcar pagada",
                    fontSize = 11.sp,
                    color = PrimaryDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DebtEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Sin deudas activas",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Pulsa + para registrar una deuda nueva",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MarkPaidDialog(
    personName: String,
    amount: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = "Marcar como pagada",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "¿Confirmas que la deuda de ${formatAmount(amount)} € con $personName ha sido saldada?",
                fontSize = 14.sp,
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Confirmar",
                    color = IncomeGreen,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteDebtContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); false } else false
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
                label = "swipe_debt_bg"
            )
            Box(
                modifier = Modifier.fillMaxSize().background(color).padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Eliminar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    ) {
        Surface(color = SurfaceWhite) { content() }
    }
}

@Composable
private fun DeleteDebtDialog(
    personName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text("Eliminar deuda", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        },
        text = {
            Text(
                "¿Seguro que quieres eliminar la deuda con $personName? Esta acción no se puede deshacer.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.Medium)
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
