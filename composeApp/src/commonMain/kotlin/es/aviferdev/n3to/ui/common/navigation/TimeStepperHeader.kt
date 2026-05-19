package es.aviferdev.n3to.ui.common.navigation

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import androidx.compose.ui.graphics.Color
import es.aviferdev.n3to.ui.common.button.IconButtonApp
import es.aviferdev.n3to.ui.common.separator.SpacerHorizontalApp
import es.aviferdev.n3to.ui.common.separator.SpacerVerticalApp

import androidx.compose.ui.tooling.preview.Preview

/**
 * Cabecera con navegación temporal (mes / año) que incluye un título
 * y botones de avance/retroceso.
 *
 * Usada en TransactionListScreen (navegación mensual) y
 * AnnualSummaryScreen (navegación anual).
 */
@Composable
fun TimeStepperHeader(
    currentValue: String,
    currentValueSecondary: String? = null,
    canGoBack: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    containerColor: Color = MaterialTheme.appColors.surface,
    dividerColor: Color = MaterialTheme.appColors.border,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .padding(horizontal = 8.dp)
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpacerHorizontalApp(8.dp)
        StepperArrowButton(enabled = canGoBack, onClick = onPrevious, label = "‹")
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentValue,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )
            if (currentValueSecondary != null) {
                Text(
                    text = currentValueSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }
        StepperArrowButton(enabled = true, onClick = onNext, label = "›")
        SpacerHorizontalApp(8.dp)
    }
    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
}

@Composable
private fun StepperArrowButton(enabled: Boolean, onClick: () -> Unit, label: String) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (enabled) MaterialTheme.appColors.surfaceElevated else androidx.compose.ui.graphics.Color.Transparent)
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            color = if (enabled) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textTertiary
        )
    }
}

@Preview
@Composable
private fun TimeStepperHeaderPreview() {
    TimeStepperHeader(
        currentValue = "Marzo",
        currentValueSecondary = "2026",
        canGoBack = true,
        onPrevious = {},
        onNext = {}
    )
}
