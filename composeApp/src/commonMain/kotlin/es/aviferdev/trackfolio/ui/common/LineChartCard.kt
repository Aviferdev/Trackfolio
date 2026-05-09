package es.aviferdev.trackfolio.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val MONTH_ABBR = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic"
)

/**
 * Componente reutilizable de gráfico de línea basado en Canvas.
 *
 * Muestra una línea con área sombreada, ejes con etiquetas y guías horizontales.
 * No usa librerías externas — todo es dibujo nativo de Compose.
 *
 * @param title título de la tarjeta.
 * @param subtitle subtítulo descriptivo.
 * @param points lista de pares (epochMillis, valor). Debe estar ordenada cronológicamente.
 * @param lineColor color de la línea y el área sombreada.
 * @param currencyCode código ISO-4217 para el símbolo de moneda.
 * @param balancesHidden si true, oculta los valores del eje Y.
 */
@Composable
fun LineChartCard(
    title: String,
    subtitle: String,
    points: List<Pair<Long, Double>>,
    lineColor: Color,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(16.dp))

            if (points.size < 2) {
                EmptyLineChartState()
            } else {
                LineChartCanvas(
                    points = points,
                    lineColor = lineColor,
                    currencyCode = currencyCode,
                    balancesHidden = balancesHidden,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

// ─── Canvas principal ─────────────────────────────────────────────────────────

@Composable
private fun LineChartCanvas(
    points: List<Pair<Long, Double>>,
    lineColor: Color,
    currencyCode: String,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val symbol = currencySymbol(currencyCode)
    val guideColor = TextSecondary.copy(alpha = 0.15f)
    val axisColor = TextSecondary.copy(alpha = 0.35f)
    val labelColor = TextSecondary
    val areaColor = lineColor.copy(alpha = 0.12f)

    val minVal = points.minOf { it.second }
    val maxVal = points.maxOf { it.second }
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    val yLabels = listOf(maxVal, (maxVal + minVal) / 2.0, minVal)

    Canvas(modifier = modifier) {
        val leftPad = 56.dp.toPx()
        val rightPad = 12.dp.toPx()
        val topPad = 8.dp.toPx()
        val bottomPad = 24.dp.toPx()
        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad

        // ── Guías horizontales ───────────────────────────────────────────
        for (i in 0..3) {
            val y = topPad + chartH * i / 3f
            drawLine(
                color = guideColor,
                start = Offset(leftPad, y),
                end = Offset(size.width - rightPad, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
            )
        }

        // ── Eje Y (izquierda) ───────────────────────────────────────────
        drawLine(
            color = axisColor,
            start = Offset(leftPad, topPad),
            end = Offset(leftPad, topPad + chartH),
            strokeWidth = 1.dp.toPx()
        )

        // ── Eje X (abajo) ───────────────────────────────────────────────
        drawLine(
            color = axisColor,
            start = Offset(leftPad, topPad + chartH),
            end = Offset(size.width - rightPad, topPad + chartH),
            strokeWidth = 1.dp.toPx()
        )

        // ── Mapear puntos a coordenadas ─────────────────────────────────
        val coords = points.mapIndexed { idx, (_, value) ->
            val x = leftPad + chartW * idx / (points.size - 1).toFloat()
            val yRatio = ((value - minVal) / range).toFloat()
            val y = topPad + chartH * (1f - yRatio)
            Offset(x, y)
        }

        // ── Área sombreada ──────────────────────────────────────────────
        val areaPath = Path().apply {
            moveTo(coords.first().x, topPad + chartH)
            coords.forEach { lineTo(it.x, it.y) }
            lineTo(coords.last().x, topPad + chartH)
            close()
        }
        drawPath(
            path = areaPath,
            color = areaColor
        )

        // ── Línea principal ─────────────────────────────────────────────
        val linePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 1 until coords.size) {
                lineTo(coords[i].x, coords[i].y)
            }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

// ─── Estado vacío ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyLineChartState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📈", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Sin datos suficientes",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Actualiza precios para ver la evolución",
                fontSize = 11.sp,
                color = TextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
