package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import es.aviferdev.n3to.ui.common.ProgressBar
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.WarnAmber
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.platform.nowLocalDate
import es.aviferdev.n3to.ui.theme.formatAmountEuro

/**
 * Tarjeta de progreso de objetivos del mes actual para la pantalla Home.
 * Muestra dos progress bars: ahorro e inversión, con checks cuando se cumplen.
 *
 * Si no hay ningún objetivo definido, muestra un placeholder con CTA.
 */
@Composable
fun GoalProgressCard(
    progress: MonthlyGoalProgress,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        if (!progress.hasAnyGoal) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToSettings)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Outlined.GpsFixed,
                    contentDescription = null,
                    tint = CyanAccent.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Define tus objetivos mensuales",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = CyanAccent
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Ahorro e inversión — Toca para configurar",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Objetivos del mes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = progress.monthLabel,
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Barra de ahorro
                if (progress.savingsTarget > 0.0) {
                    GoalProgressRow(
                        icon = Icons.Outlined.GpsFixed,
                        label = "Ahorro",
                        target = progress.savingsTarget,
                        actual = progress.savingsActual,
                        achieved = progress.savingsAchieved,
                        progress = progress.savingsProgress
                    )
                    Spacer(Modifier.height(10.dp))
                }

                // Barra de inversión
                if (progress.investmentTarget > 0.0) {
                    GoalProgressRow(
                        icon = Icons.AutoMirrored.Outlined.ShowChart,
                        label = "Inversión",
                        target = progress.investmentTarget,
                        actual = progress.investmentActual,
                        achieved = progress.investmentAchieved,
                        progress = progress.investmentProgress
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalProgressRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    target: Double,
    actual: Double,
    achieved: Boolean,
    progress: Float,
) {
    val projection = rememberProjection(target, actual)

    Column(modifier = Modifier.fillMaxWidth()) {
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = if (achieved) "✅ ${formatAmountEuro(actual)}" else "${formatAmountEuro(actual)} / ${
                    formatAmountEuro(
                        target
                    )
                }",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (achieved) IncomeGreen else TextTertiary
            )
        }

        Spacer(Modifier.height(6.dp))

        val progressColor = when {
            achieved -> IncomeGreen
            progress >= 0.5f -> WarnAmber
            else -> ExpenseRed
        }

        ProgressBar(
            progress = progress,
            color = progressColor,
            height = 6.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // Proyección
        if (!achieved && target > 0.0) {
            Spacer(Modifier.height(4.dp))
            projection?.let { proj ->
                Text(
                    text = proj,
                    fontSize = 10.sp,
                    color = TextTertiary,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Calcula la proyección mensual basada en el ritmo actual.
 * @return texto descriptivo o null si no se puede calcular.
 */
@Composable
private fun rememberProjection(target: Double, actual: Double): String? {
    val today = nowLocalDate()
    val dayOfMonth = today.dayOfMonth
    val daysInMonth = daysInMonth(today.year, today.monthNumber)

    // Necesitamos al menos 1 día transcurrido y valor > 0
    if (dayOfMonth < 1 || daysInMonth < 1) return null
    if (actual <= 0.0) return null

    val daysPassed = dayOfMonth
    val daysRemaining = daysInMonth - dayOfMonth
    val dailyAverage = actual / daysPassed

    val projected = dailyAverage * daysInMonth
    if (projected <= 0.0) return null

    return if (projected >= target) {
        val margin = projected - target
        "📈 Proyección: ${formatAmountEuro(projected)} — por encima del objetivo (+${
            formatAmountEuro(
                margin
            )
        })"
    } else if (dailyAverage > 0.0) {
        val neededDaily = (target - actual) / daysRemaining
        "📊 Ritmo actual: ${formatAmount(dailyAverage)}€/día — Necesitas ${formatAmount(neededDaily)}€/día para alcanzar el objetivo"
    } else {
        "📊 Aún no hay movimiento este mes"
    }
}

/** Días totales de un mes (considerando bisiestos). Compatible con iOS. */
private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 30
}

/** Nombre del mes en español para mostrar en la tarjeta. */
private val MonthlyGoalProgress.monthLabel: String
    get() = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    ).getOrElse(month.toIntOrNull()?.minus(1) ?: 0) { "" }
