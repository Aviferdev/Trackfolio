package es.aviferdev.n3to.ui.annual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.MonthlyGoalProgress
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary

/**
 * Tarjeta de resumen anual de cumplimiento de objetivos.
 * Muestra cuántos meses se ha cumplido cada objetivo (ahorro e inversión)
 * con indicadores visuales por mes (✅ / ❌ / -).
 */
@Composable
fun GoalSummaryCard(
    goalProgress: List<MonthlyGoalProgress>,
    modifier: Modifier = Modifier
) {
    if (goalProgress.isEmpty()) return

    val hasAnyGoal = goalProgress.any { it.hasAnyGoal }
    if (!hasAnyGoal) return

    val savingsAchieved = goalProgress.count { it.savingsAchieved }
    val savingsTotal = goalProgress.count { it.savingsTarget > 0.0 }
    val investmentAchieved = goalProgress.count { it.investmentAchieved }
    val investmentTotal = goalProgress.count { it.investmentTarget > 0.0 }

    val allMonthsHaveSavingsGoal = savingsTotal == 12
    val allMonthsHaveInvestmentGoal = investmentTotal == 12

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.GpsFixed,
                    contentDescription = null,
                    tint = PrimaryDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Cumplimiento de objetivos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Leyenda de indicadores
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoalLegendItem(color = IncomeGreen, label = "Cumplido")
                GoalLegendItem(color = ExpenseRed, label = "No cumplido")
                GoalLegendItem(color = TextTertiary, label = "Sin objetivo")
            }

            Spacer(Modifier.height(12.dp))

            // Indicador de ahorro
            if (savingsTotal > 0) {
                GoalTypeSummaryRow(
                    icon = Icons.Outlined.GpsFixed,
                    label = "Ahorro",
                    achieved = savingsAchieved,
                    total = if (allMonthsHaveSavingsGoal) 12 else savingsTotal
                )
                Spacer(Modifier.height(6.dp))
                MonthIndicatorRow(
                    goalProgress = goalProgress,
                    isSavings = true
                )
                Spacer(Modifier.height(12.dp))
            }

            // Indicador de inversión
            if (investmentTotal > 0) {
                GoalTypeSummaryRow(
                    icon = Icons.AutoMirrored.Outlined.ShowChart,
                    label = "Inversión",
                    achieved = investmentAchieved,
                    total = if (allMonthsHaveInvestmentGoal) 12 else investmentTotal
                )
                Spacer(Modifier.height(6.dp))
                MonthIndicatorRow(
                    goalProgress = goalProgress,
                    isSavings = false
                )
            }
        }
    }
}

@Composable
private fun GoalLegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, color = TextTertiary)
    }
}

@Composable
private fun GoalTypeSummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    achieved: Int,
    total: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Text(
            text = "$achieved / $total meses",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (achieved == total && total > 0) IncomeGreen else TextSecondary
        )
    }
}

@Composable
private fun MonthIndicatorRow(
    goalProgress: List<MonthlyGoalProgress>,
    isSavings: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        goalProgress.forEach { progress ->
            val status = if (isSavings) {
                if (progress.savingsTarget <= 0.0) GoalStatus.NO_GOAL
                else if (progress.savingsAchieved) GoalStatus.ACHIEVED
                else GoalStatus.FAILED
            } else {
                if (progress.investmentTarget <= 0.0) GoalStatus.NO_GOAL
                else if (progress.investmentAchieved) GoalStatus.ACHIEVED
                else GoalStatus.FAILED
            }

            val color = when (status) {
                GoalStatus.ACHIEVED -> IncomeGreen
                GoalStatus.FAILED -> ExpenseRed
                GoalStatus.NO_GOAL -> TextTertiary
            }
            val symbol = when (status) {
                GoalStatus.ACHIEVED -> "✓"
                GoalStatus.FAILED -> "✗"
                GoalStatus.NO_GOAL -> "–"
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

private enum class GoalStatus { ACHIEVED, FAILED, NO_GOAL }
