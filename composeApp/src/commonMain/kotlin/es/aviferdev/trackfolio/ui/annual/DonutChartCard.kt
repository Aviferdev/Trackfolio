package es.aviferdev.trackfolio.ui.annual

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.formatAmountWithCurrency
import es.aviferdev.trackfolio.ui.theme.maskAmount
import kotlin.math.abs

/**
 * Componente de gráfico donut reutilizable.
 * Muestra un gráfico donut con leyenda y total en el centro.
 */
@Composable
fun DonutChartCard(
    title: String,
    subtitle: String,
    slices: List<DonutSlice>,
    totalAmount: Double,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    if (slices.isEmpty()) {
        EmptyDonutState(title = title, subtitle = subtitle)
        return
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = subtitle,
                fontSize = 11.sp,
                color    = TextSecondary
            )

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // ── Donut ────────────────────────────────────────────────
                Box(
                    modifier         = Modifier
                        .size(120.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutCanvas(
                        slices   = slices,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                    )
                    // Total al centro
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text     = "Total",
                            fontSize = 10.sp,
                            color    = TextSecondary
                        )
                        Text(
                            text       = maskAmount(
                                formatAmountWithCurrency(totalAmount, currencyCode),
                                balancesHidden
                            ),
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextPrimary
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // ── Leyenda ─────────────────────────────────────────────
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.forEach { slice ->
                        DonutLegendRow(slice = slice, currencyCode = currencyCode, balancesHidden = balancesHidden)
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutCanvas(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val side       = minOf(size.width, size.height)
        val strokeW    = side * 0.22f
        val padding    = strokeW / 2f
        val arcSize    = Size(side - strokeW, side - strokeW)
        val topLeft    = Offset(padding, padding)
        val gapDeg     = 1.5f
        var startAngle = -90f

        slices.forEach { slice ->
            val sweep = (slice.percent.toFloat() * 360f / 100f) - gapDeg
            if (sweep > 0f) {
                drawArc(
                    color     = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(width = strokeW)
                )
            }
            startAngle += (slice.percent.toFloat() * 360f / 100f)
        }
    }
}

@Composable
private fun DonutLegendRow(slice: DonutSlice, currencyCode: String, balancesHidden: Boolean) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(slice.color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text     = slice.icon,
            fontSize = 12.sp
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text     = slice.name,
            fontSize = 12.sp,
            color    = TextPrimary,
            modifier = Modifier.weight(1f, fill = true),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
Text(
                            text       = maskAmount(formatAmountWithCurrency(slice.amount, currencyCode), balancesHidden),
                            fontSize   = 11.sp,
                            color      = TextSecondary,
                            fontWeight = FontWeight.Normal,
                            modifier   = Modifier.wrapContentSize()
                        )
        Spacer(Modifier.width(6.dp))
        Text(
            text       = "(${formatPercentLegend(slice.percent)}%)",
            fontSize   = 11.sp,
            color      = TextSecondary.copy(alpha = 0.7f),
            fontWeight = FontWeight.Normal,
            modifier   = Modifier.wrapContentSize()
        )
    }
}

@Composable
private fun EmptyDonutState(title: String, subtitle: String) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = title,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = subtitle,
                fontSize = 11.sp,
                color    = TextSecondary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text     = "📊",
                fontSize = 32.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text     = "Sin datos para este año",
                fontSize = 13.sp,
                color    = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatPercentLegend(value: Double): String {
    val rounded = (value * 10.0).toLong()
    return "${rounded / 10},${rounded % 10}"
}