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
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.EmergencyFundMethod
import es.aviferdev.n3to.domain.model.EmergencyFundStatus
import es.aviferdev.n3to.ui.common.ProgressBar
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmountEuro
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.home_ef_configure_subtitle
import n3to.composeapp.generated.resources.home_ef_configure_title
import n3to.composeapp.generated.resources.home_ef_covered
import n3to.composeapp.generated.resources.home_ef_current_balance
import n3to.composeapp.generated.resources.home_ef_missing_format
import n3to.composeapp.generated.resources.home_ef_monthly_avg_format
import n3to.composeapp.generated.resources.home_ef_months
import n3to.composeapp.generated.resources.home_ef_target
import n3to.composeapp.generated.resources.home_section_emergency_fund
import org.jetbrains.compose.resources.stringResource

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
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToSettings),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
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
                    tint = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.home_ef_configure_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.appColors.cyanAccent
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.home_ef_configure_subtitle),
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
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
                        tint = if (status.isCovered) MaterialTheme.appColors.income else MaterialTheme.appColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.home_section_emergency_fund),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
                Text(
                    text = if (status.isCovered) stringResource(Res.string.home_ef_covered) else stringResource(
                        Res.string.home_ef_months,
                        status.targetMonths
                    ),
                    fontSize = 11.sp,
                    color = if (status.isCovered) MaterialTheme.appColors.income else MaterialTheme.appColors.textTertiary
                )
            }

            Spacer(Modifier.height(10.dp))

            // Barra de progreso
            val progressColor = when {
                status.isCovered -> MaterialTheme.appColors.income
                status.coveragePercentage >= 0.5f -> MaterialTheme.appColors.warnAmber
                else -> MaterialTheme.appColors.expense
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
                        text = stringResource(Res.string.home_ef_current_balance),
                        fontSize = 10.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )
                    Text(
                        text = formatAmountEuro(status.currentBalance),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Res.string.home_ef_target),
                        fontSize = 10.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )
                    Text(
                        text = formatAmountEuro(status.targetAmount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
            }

            // Mensajes adicionales
            if (status.missingAmount > 0.0 && !status.isCovered) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        Res.string.home_ef_missing_format,
                        formatAmountEuro(status.missingAmount)
                    ),
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.expense,
                    fontWeight = FontWeight.Medium
                )
            }

            // Media mensual (solo AUTO)
            if (status.calculationMethod == EmergencyFundMethod.AUTO && status.monthlyAverage != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        Res.string.home_ef_monthly_avg_format,
                        formatAmountEuro(status.monthlyAverage)
                    ),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }

            // Mensaje de cálculo (ej: errores, sin datos)
            status.calculationMessage?.let { msg ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = msg,
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.expense
                )
            }
        }
    }
}
