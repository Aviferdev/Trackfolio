package es.aviferdev.n3to.ui.common.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.separator.SpacerHorizontalApp
import es.aviferdev.n3to.ui.common.separator.SpacerVerticalApp
import es.aviferdev.n3to.ui.theme.appColors
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
    currentValue: String,
    currentValueSecondary: String? = null,
    canGoBack: Boolean,
    canGoForward: Boolean = true,
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
        StepperArrowButton(
            enabled = canGoBack,
            onClick = onPrevious,
            icon = Icons.Default.ChevronLeft,
            modifier = Modifier.alpha(if (canGoBack) 1f else 0f)
        )

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
        StepperArrowButton(
            enabled = canGoForward,
            onClick = onNext,
            icon = Icons.Default.ChevronRight,
            modifier = Modifier.alpha(if (canGoForward) 1f else 0f)
        )
        SpacerHorizontalApp(8.dp)
    }
    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
}

@Composable
fun StepperArrowButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textTertiary
        )
    }
}
