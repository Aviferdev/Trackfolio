package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.EmergencyFundMethod
import es.aviferdev.n3to.domain.model.EmergencyFundStatus
import es.aviferdev.n3to.ui.common.ProgressBar
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.WarnAmber
import es.aviferdev.n3to.ui.theme.formatAmountEuro

/**
 * Tarjeta del fondo de emergencia para la Home.
 *
 * Si no está configurado, muestra un placeholder compacto que invita
 * a configurarlo. Si está configurado, muestra el progreso de cobertura.
 */
@Composable
fun EmergencyFundCard(
    status: EmergencyFundStatus,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!status.isConfigured) {
        // Placeholder: invitar a configurar
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToSettings),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = PrimaryDark.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Configura tu fondo de emergencia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryDark
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Mantén tu saldo seguro — Toca para configurar",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }
        return
    }

    // ── Configurado: mostrar progreso ──
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = if (status.isCovered) IncomeGreen else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Fondo de emergencia",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = if (status.isCovered) "Cubierto ✅" else "${status.targetMonths} meses",
                    fontSize = 11.sp,
                    color = if (status.isCovered) IncomeGreen else TextTertiary
                )
            }

            Spacer(Modifier.height(10.dp))

            // Barra de progreso
            val progressColor = when {
                status.isCovered -> IncomeGreen
                status.coveragePercentage >= 0.5f -> WarnAmber
                else -> ExpenseRed
            }

            ProgressBar(
                progress = status.coveragePercentage,
                color = progressColor,
                height = 6.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Detalles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Saldo actual",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                    Text(
                        text = formatAmountEuro(status.currentBalance),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Objetivo",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                    Text(
                        text = formatAmountEuro(status.targetAmount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }

            // Mensajes adicionales
            if (status.missingAmount > 0.0 && !status.isCovered) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Te faltan ${formatAmountEuro(status.missingAmount)} para cubrir el fondo",
                    fontSize = 11.sp,
                    color = ExpenseRed,
                    fontWeight = FontWeight.Medium
                )
            }

            // Media mensual (solo AUTO)
            if (status.calculationMethod == EmergencyFundMethod.AUTO && status.monthlyAverage != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Gasto medio: ${formatAmountEuro(status.monthlyAverage)}/mes",
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }

            // Mensaje de cálculo (ej: errores, sin datos)
            status.calculationMessage?.let { msg ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = msg,
                    fontSize = 10.sp,
                    color = ExpenseRed
                )
            }
        }
    }
}
