package es.aviferdev.n3to.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.SwapHoriz
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
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// ═══════════════════════════════════════════════════════════════════════════════
// Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onBack: () -> Unit = {},
    onEditTransaction: ((Transaction) -> Unit)? = null,
    viewModel: TransactionDetailViewModel = koinViewModel { parametersOf(transactionId) }
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Observar estado Deleted para navegar back
    LaunchedEffect(uiState) {
        if (uiState is TransactionDetailUiState.Deleted) {
            onBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        TopBarApp(
            title = "Detalle",
            navigateBack = onBack
        )

        when (val state = uiState) {
            is TransactionDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryDark)
                }
            }

            is TransactionDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = ExpenseRed,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            is TransactionDetailUiState.Success -> {
                TransactionDetailContent(
                    transaction    = state.transaction,
                    categoryName   = state.categoryName,
                    accountName    = state.accountName,
                    incomeTypeLabel = state.incomeTypeLabel,
                    incomeTypeEmoji = state.incomeTypeEmoji,
                    onEdit         = if (state.transaction.isLinkedToAsset) null
                                     else { onEditTransaction?.let { { it(state.transaction) } } },
                    onDelete       = { showDeleteDialog = true }
                )
            }

            is TransactionDetailUiState.Deleted -> {
                // Navegación ya manejada en LaunchedEffect
            }
        }
    }

    // ── Delete confirmation dialog ─────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = SurfaceWhite,
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
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTransaction()
                }) {
                    Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Content
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun TransactionDetailContent(
    transaction: Transaction,
    categoryName: String,
    accountName: String,
    incomeTypeLabel: String?,
    incomeTypeEmoji: String?,
    onEdit: (() -> Unit)?,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        // ── Card superior con tipo, importe y categoría ────────────────────
        val (typeLabel, typeColor) = when {
            transaction.isAdjustment -> "AJUSTE" to PrimaryDark
            transaction.isLinkedToAsset -> "INVERSIÓN" to PrimaryDark
            transaction.isIncome  -> "INGRESO" to IncomeGreen
            else                  -> "GASTO" to ExpenseRed
        }
        val isNegativeAmount = when {
            transaction.isAdjustment -> transaction.amount < 0
            transaction.isIncome    -> false
            else                    -> true
        }
        val absAmount = kotlin.math.abs(transaction.amount)
        val amountPrefix = if (isNegativeAmount) "−" else "+"

        val categoryIcon = when {
            transaction.isAdjustment   -> Icons.Outlined.SwapHoriz
            transaction.isLinkedToAsset -> Icons.Outlined.ShowChart
            transaction.isIncome       -> Icons.Outlined.ArrowDownward
            else                       -> Icons.Outlined.ArrowUpward
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = typeColor.copy(alpha = 0.10f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(typeColor.copy(alpha = 0.20f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = typeLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Amount
                Text(
                    text = "$amountPrefix${formatAmountAbs(absAmount)} €",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                // Category line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = categoryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Details section ────────────────────────────────────────────────
        DetailSectionHeader("Detalles")

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                // Categoría
                DetailRow(
                    label = "Categoría",
                    value = categoryName,
                    isLast = false
                )

                // Cuenta
                DetailRow(
                    label = "Cuenta",
                    value = accountName,
                    isLast = false
                )

                // Fecha
                DetailRow(
                    label = "Fecha",
                    value = formatDetailDate(transaction.date),
                    isLast = false
                )

                // Notas (si existen)
                val notes = transaction.notes
                if (!notes.isNullOrBlank()) {
                    DetailRow(
                        label = "Notas",
                        value = notes,
                        isLast = !shouldShowIncomeDetails(transaction)
                    )
                }

                // ── Income-specific fields ──────────────────────────────────
                if (shouldShowIncomeDetails(transaction)) {
                    val grossAmount = transaction.grossAmount
                    if (grossAmount != null) {
                        DetailRow(
                            label = "Importe bruto",
                            value = "${formatAmount(grossAmount)} €",
                            isLast = false
                        )
                    }
                    transaction.taxLines.forEach { taxLine ->
                        val lineText = if (taxLine.percent != null) {
                            "${taxLine.percent}% (${formatAmount(taxLine.amount)} €)"
                        } else {
                            "${formatAmount(taxLine.amount)} €"
                        }
                        DetailRow(label = taxLine.name, value = lineText, isLast = false)
                    }
                    val commissionAmount = transaction.commissionAmount
                    if (commissionAmount != null && commissionAmount > 0) {
                        DetailRow(
                            label = "Comisiones",
                            value = "${formatAmount(commissionAmount)} €",
                            isLast = false
                        )
                    }
                    val issuerName = transaction.issuerName
                    if (!issuerName.isNullOrBlank()) {
                        DetailRow(
                            label = "Emisor",
                            value = issuerName,
                            isLast = true
                        )
                    }
                }

                // Si no hay notas ni income details, Fecha será el último elemento visible.
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── Action buttons ─────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Editar (solo si no es linkedToAsset)
            if (onEdit != null) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryDark
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryDark)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Editar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Eliminar (solo si no es linkedToAsset)
            if (!transaction.isLinkedToAsset) {
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseRed,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Eliminar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Para linked-to-asset: mostrar indicador de portfolio
                Surface(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryDark.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "Movimiento del Portfolio",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryDark
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Helper Composables
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun DetailSectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextTertiary,
        letterSpacing = 0.7.sp
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isLast: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.width(100.dp)
            )
            // Value — alineado a la izquierda
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 14.dp),
                color = BorderGray,
                thickness = 0.5.dp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Helpers
// ═══════════════════════════════════════════════════════════════════════════════

/** Determina si la transacción tiene campos de ingreso que mostrar. */
private fun shouldShowIncomeDetails(transaction: Transaction): Boolean {
    if (!transaction.isIncome) return false
    return transaction.grossAmount != null
            || transaction.taxLines.isNotEmpty()
            || transaction.commissionAmount != null
            || !transaction.issuerName.isNullOrBlank()
}

/** Formatea el valor absoluto sin signo: "1.500,00" */
private fun formatAmountAbs(amount: Double): String {
    val rounded = (amount * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val eurosStr = euros.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$eurosStr,${cents.toString().padStart(2, '0')}"
}

/** Formatea fecha larga: "12 de mayo de 2026" */
private fun formatDetailDate(epochMillis: Long): String {
    val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val ld = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}
