package es.aviferdev.n3to.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.PriceSource
import es.aviferdev.n3to.ui.theme.appColors

/**
 * Badge que indica el origen del precio de un activo.
 * - [PriceSource.AUTO]: badge azul/verde con "Auto" y un icono de sincronización.
 * - [PriceSource.MANUAL]: badge gris con "Manual" y un icono de lápiz.
 *
 * También puede mostrar un estado de error de validación del ISIN.
 */
@Composable
fun PriceSourceBadge(
    priceSource: PriceSource,
    isinValidationError: String? = null,
    modifier: Modifier = Modifier
) {
    val hasError = isinValidationError != null
    val backgroundColor: Color
    val textColor: Color
    val label: String
    val icon: String

    when {
        hasError -> {
            backgroundColor = MaterialTheme.appColors.expense.copy(alpha = 0.15f)
            textColor = MaterialTheme.appColors.expense
            label = "Error"
            icon = "⚠️"
        }

        priceSource == PriceSource.AUTO -> {
            backgroundColor = Color(0xFF1B5E20).copy(alpha = 0.15f)
            textColor = Color(0xFF4CAF50)
            label = "Auto"
            icon = "🔄"
        }

        else -> {
            backgroundColor = Color(0xFF424242).copy(alpha = 0.15f)
            textColor = MaterialTheme.appColors.textSecondary
            label = "Manual"
            icon = "✏️"
        }
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (hasError) "⚠️" else icon,
                fontSize = 9.sp
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Versión simplificada del badge que solo muestra un indicador visual pequeño
 * al lado del precio.
 */
@Composable
fun PriceSourceIndicator(
    priceSource: PriceSource,
    modifier: Modifier = Modifier
) {
    val color = when (priceSource) {
        PriceSource.AUTO -> Color(0xFF4CAF50)
        PriceSource.MANUAL -> MaterialTheme.appColors.textSecondary
    }
    val label = when (priceSource) {
        PriceSource.AUTO -> "↻"
        PriceSource.MANUAL -> "✎"
    }

    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}
