package es.aviferdev.n3to.ui.transaction

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.automirrored.outlined.ShowChart
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
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_category_label
import n3to.composeapp.generated.resources.common_date_label
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.common_retry
import n3to.composeapp.generated.resources.fiscal_commissions_short
import n3to.composeapp.generated.resources.transaction_delete_message
import n3to.composeapp.generated.resources.transaction_detail_account_label
import n3to.composeapp.generated.resources.transaction_detail_delete_cd
import n3to.composeapp.generated.resources.transaction_detail_edit_cd
import n3to.composeapp.generated.resources.transaction_detail_issuer_label
import n3to.composeapp.generated.resources.transaction_detail_notes_label
import n3to.composeapp.generated.resources.transaction_detail_title
import org.jetbrains.compose.resources.stringResource
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.background)) {
        TopBarApp(
            title = stringResource(Res.string.transaction_detail_title),
            navigateBack = onBack
        )

        when (val state = uiState) {
            is TransactionDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.appColors.primary)
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
                            color = MaterialTheme.appColors.expense,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.appColors.primary)
                        ) {
                            Text(stringResource(Res.string.common_retry))
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
            containerColor   = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.transaction_detail_title) + " " + stringResource(Res.string.common_delete).lowercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(Res.string.transaction_delete_message),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTransaction()
                }) {
                    Text(stringResource(Res.string.common_delete), color = MaterialTheme.appColors.expense, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.common_cancel), color = MaterialTheme.appColors.primary, fontWeight = FontWeight.Medium)
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
        val isPropertyTransaction = transaction.linkedPropertyId != null
        val (typeLabel, typeColor) = when {
            transaction.isAdjustment -> "AJUSTE" to MaterialTheme.appColors.primary
            transaction.isLinkedToAsset -> "INVERSIÓN" to MaterialTheme.appColors.primary
            isPropertyTransaction   -> "INMUEBLE" to MaterialTheme.appColors.income
            transaction.isIncome  -> "INGRESO" to MaterialTheme.appColors.income
            else                  -> "GASTO" to MaterialTheme.appColors.expense
        }
        val isLinkedToProperty = transaction.linkedPropertyId != null
    val isNegativeAmount = when {
            transaction.isAdjustment -> transaction.amount < 0
            transaction.isIncome    -> false
            else                    -> true
        }
        val absAmount = kotlin.math.abs(transaction.amount)
        val amountPrefix = if (isNegativeAmount) "−" else "+"

        val categoryIcon = when {
            transaction.isAdjustment   -> Icons.Outlined.SwapHoriz
            transaction.isLinkedToAsset -> Icons.AutoMirrored.Outlined.ShowChart
            isLinkedToProperty         -> Icons.Outlined.House
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
                        tint = MaterialTheme.appColors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = categoryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.appColors.textPrimary
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                // Categoría
                DetailRow(
                    label = stringResource(Res.string.common_category_label),
                    value = categoryName,
                    isLast = false
                )

                // Cuenta
                DetailRow(
                    label = stringResource(Res.string.transaction_detail_account_label),
                    value = accountName,
                    isLast = false
                )

                // Fecha
                DetailRow(
                    label = stringResource(Res.string.common_date_label),
                    value = formatDetailDate(transaction.date),
                    isLast = false
                )

                // Notas (si existen)
                val notes = transaction.notes
                if (!notes.isNullOrBlank()) {
                    DetailRow(
                        label = stringResource(Res.string.transaction_detail_notes_label),
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
                            label = stringResource(Res.string.fiscal_commissions_short),
                            value = "${formatAmount(commissionAmount)} €",
                            isLast = false
                        )
                    }
                    val issuerName = transaction.issuerName
                    if (!issuerName.isNullOrBlank()) {
                        DetailRow(
                            label = stringResource(Res.string.transaction_detail_issuer_label),
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
            val isPropertyTx = transaction.linkedPropertyId != null
            // Editar (solo si no es linkedToAsset ni linkedToProperty)
            if (onEdit != null && !isPropertyTx) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.appColors.primary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.appColors.primary)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(Res.string.transaction_detail_edit_cd),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(Res.string.transaction_detail_edit_cd),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            val isPropertyTxFinal = transaction.linkedPropertyId != null
            // Eliminar (solo si no es linkedToAsset ni linkedToProperty)
            if (!transaction.isLinkedToAsset && !isPropertyTxFinal) {
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.appColors.expense,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(Res.string.transaction_detail_delete_cd),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(Res.string.transaction_detail_delete_cd),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Para linked-to-asset o linked-to-property: mostrar indicador
                val linkedLabel = when {
                    transaction.isLinkedToAsset -> "Movimiento del Portfolio"
                    isPropertyTxFinal -> "Gestionable desde la propiedad"
                    else -> "Movimiento vinculado"
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.appColors.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            linkedLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.appColors.primary
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
        color = MaterialTheme.appColors.textTertiary,
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
                color = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.width(100.dp)
            )
            // Value — alineado a la izquierda
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
        }
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 14.dp),
                color = MaterialTheme.appColors.border,
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
