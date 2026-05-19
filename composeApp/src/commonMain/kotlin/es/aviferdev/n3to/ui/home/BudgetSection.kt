package es.aviferdev.n3to.ui.home

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.CategoryBudgetStatus
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.ui.common.ProgressBar
import es.aviferdev.n3to.ui.theme.*
import es.aviferdev.n3to.ui.theme.N3toTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * Sección de presupuestos para mostrar en Home.
 * Muestra una tarjeta con el progreso de cada categoría que tiene límite anual,
 * o un estado vacío con CTA para empezar a configurar.
 */
@Composable
fun BudgetSection(
    statuses: List<CategoryBudgetStatus>,
    onEditBudget: (categoryId: String, categoryName: String, currentLimit: Double, currentLimitType: LimitType) -> Unit = { _: String, _: String, _: Double, _: LimitType -> },
    onConfigureBudgets: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(11.dp)),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (statuses.isEmpty()) {
                EmptyBudgetCard(onConfigure = onConfigureBudgets)
            } else {
                statuses.forEachIndexed { index, status ->
                    BudgetRow(
                        status = status,
                        onEdit = {
                            onEditBudget(
                                status.categoryId,
                                status.categoryName,
                                status.annualLimit,
                                status.limitType
                            )
                        }
                    )
                    if (index < statuses.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.appColors.navyBorder,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetRow(
    status: CategoryBudgetStatus,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        status.isOverBudget -> MaterialTheme.appColors.expense
        status.isNearLimit -> MaterialTheme.appColors.warnAmber
        else -> MaterialTheme.appColors.income
    }

    val noIncome = status.limitType == LimitType.PERCENTAGE && status.totalIncome <= 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Fila superior: nombre + progreso numérico + editar ──────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status.categoryName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (!noIncome) {
                Text(
                    text = "${(status.progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = barColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            // Botón de editar límite inline
            Text(
                text = "✏️",
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable(onClick = onEdit)
                    .padding(4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Barra de progreso ──────────────────────────────────────────────────
        if (!noIncome && status.effectiveLimit > 0.0) {
            ProgressBar(
                progress = status.progress.coerceAtMost(1f),
                color = barColor,
                height = 6.dp
            )
        } else if (noIncome) {
            Text(
                text = "Sin ingresos este año",
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textTertiary,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(Modifier.height(4.dp))

        // ── Fila inferior: gastado / límite ────────────────────────────────────
        if (!noIncome && status.effectiveLimit > 0.0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatAmount(status.spent)} de ${formatAmount(status.effectiveLimit)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
                Text(
                    text = "Restan ${formatAmount(status.remaining)}",
                    fontSize = 11.sp,
                    color = if (status.isOverBudget) MaterialTheme.appColors.expense else MaterialTheme.appColors.textTertiary
                )
            }
        }

        // ── Badge de excedido ──────────────────────────────────────────────────
        if (status.isOverBudget) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "¡Límite excedido!",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.expense
            )
        }
    }
}

@Composable
private fun EmptyBudgetCard(
    onConfigure: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "💰",
            fontSize = 32.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Sin presupuestos",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.textPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Configura límites de gasto por categoría\npara controlar mejor tus finanzas",
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = onConfigure
        ) {
            Text(
                "Configurar",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.cyanAccent
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

/**
 * Formatea un valor double a moneda local (simplificado).
 */
private fun formatAmount(amount: Double): String {
    val absAmount = kotlin.math.abs(amount)
    val cents = ((absAmount * 100).toLong() % 100).toInt()
    val euros = absAmount.toLong()
    return "${euros},${cents.toString().padStart(2, '0')} €"
}

// ─── PREVIEWS ────────────────────────────────────────────────────────────────────
@Preview
@Composable
private fun BudgetSectionWithDataPreview() {
    N3toTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BudgetSection(
                statuses = listOf(
                    CategoryBudgetStatus(
                        categoryId = "1",
                        categoryName = "Alimentación",
                        annualLimit = 6000.0,
                        limitType = LimitType.FIXED,
                        spent = 3200.0,
                        totalIncome = 0.0,
                        year = "2026"
                    ),
                    CategoryBudgetStatus(
                        categoryId = "2",
                        categoryName = "Transporte",
                        annualLimit = 10.0,
                        limitType = LimitType.PERCENTAGE,
                        spent = 950.0,
                        totalIncome = 12000.0,
                        year = "2026"
                    ),
                    CategoryBudgetStatus(
                        categoryId = "3",
                        categoryName = "Ocio",
                        annualLimit = 2400.0,
                        limitType = LimitType.FIXED,
                        spent = 2100.0,
                        totalIncome = 0.0,
                        year = "2026"
                    ),
                    CategoryBudgetStatus(
                        categoryId = "4",
                        categoryName = "Ropa",
                        annualLimit = 1500.0,
                        limitType = LimitType.FIXED,
                        spent = 1600.0,
                        totalIncome = 0.0,
                        year = "2026"
                    )
                )
            )
        }
    }
}

@Preview
@Composable
private fun BudgetSectionEmptyPreview() {
    N3toTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            BudgetSection(
                statuses = emptyList()
            )
        }
    }
}
