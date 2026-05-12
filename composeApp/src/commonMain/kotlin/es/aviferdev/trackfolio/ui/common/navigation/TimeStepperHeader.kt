package es.aviferdev.trackfolio.ui.common.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.common.button.IconButtomApp
import es.aviferdev.trackfolio.ui.common.separator.SpacerHorizontalApp
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.SurfaceElevated
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Cabecera con navegación temporal (mes / año) que incluye un título
 * y botones de avance/retroceso.
 *
 * Usada en TransactionListScreen (navegación mensual) y
 * AnnualSummaryScreen (navegación anual).
 */
@Composable
fun TimeStepperHeader(
    title: String,
    currentValue: String,
    currentValueSecondary: String? = null,
    canGoBack: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    navigateBack: (() -> Unit)? = null
) {
    Surface(color = SurfaceWhite) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de retroceso
                navigateBack?.let {
                    IconButtomApp(
                        clickButton = it,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                    SpacerHorizontalApp(8.dp)
                }

                // Título
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepperArrowButton(enabled = canGoBack, onClick = onPrevious, label = "‹")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentValue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (currentValueSecondary != null) {
                        Text(
                            text = currentValueSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextTertiary
                        )
                    }
                }
                StepperArrowButton(enabled = true, onClick = onNext, label = "›")
            }
        }
        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
    }
}

@Composable
private fun StepperArrowButton(enabled: Boolean, onClick: () -> Unit, label: String) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (enabled) SurfaceElevated else androidx.compose.ui.graphics.Color.Transparent)
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            color = if (enabled) TextPrimary else TextTertiary
        )
    }
}

@Preview
@Composable
private fun TimeStepperHeaderPreview() {
    TimeStepperHeader(
        title = "Movimientos",
        currentValue = "Marzo",
        currentValueSecondary = "2026",
        canGoBack = true,
        onPrevious = {},
        onNext = {}
    )
}
