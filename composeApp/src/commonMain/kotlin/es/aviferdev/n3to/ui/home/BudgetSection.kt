package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
 * Muestra una tarjeta con el progreso de cada categoría que tiene límite anual.
 */
@Composable
fun BudgetSection(
    statuses: List<CategoryBudgetStatus>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, NavyBorder, RoundedCornerShape(11.dp)),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            statuses.forEachIndexed { index, status ->
                BudgetRow(status = status)
                if (index < statuses.lastIndex) {
                    HorizontalDivider(
                        color = NavyBorder,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetRow(
    status: CategoryBudgetStatus,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        status.isOverBudget -> ExpenseRed
        status.isNearLimit -> WarnAmber
        else -> IncomeGreen
    }

    // Si es porcentaje y no hay ingresos, no podemos calcular
    val noIncome = status.limitType == LimitType.PERCENTAGE && status.totalIncome <= 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Fila superior: nombre + progreso numérico ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status.categoryName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (!noIncome) {
                Text(
                    text = "${(status.progress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = barColor
                )
            }
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
                color = TextTertiary,
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
                    color = TextSecondary
                )
                Text(
                    text = "Restan ${formatAmount(status.remaining)}",
                    fontSize = 11.sp,
                    color = if (status.isOverBudget) ExpenseRed else TextTertiary
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
                color = ExpenseRed
            )
        }
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
private fun BudgetSectionPreview() {
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
