package es.aviferdev.n3to.ui.common

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

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
 * @param balancesHidden si true, oculta los valores del eje Y.
 */
@Composable
fun LineChartCard(
    title: String,
    subtitle: String,
    points: List<Pair<Long, Double>>,
    lineColor: Color,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier,
    rotateXLabels: Boolean = false,
    timeRangeSelector: (@Composable () -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            if (timeRangeSelector != null) {
                Spacer(Modifier.height(10.dp))
                timeRangeSelector()
            }

            Spacer(Modifier.height(16.dp))

            if (points.size < 2) {
                EmptyLineChartState()
            } else {
                LineChartCanvas(
                    points = points,
                    lineColor = lineColor,
                    balancesHidden = balancesHidden,
                    textMeasurer = textMeasurer,
                    rotateXLabels = rotateXLabels,
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
    balancesHidden: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    rotateXLabels: Boolean = false,
    modifier: Modifier = Modifier
) {
    val guideColor = MaterialTheme.appColors.textSecondary.copy(alpha = 0.15f)
    val axisColor = MaterialTheme.appColors.textSecondary.copy(alpha = 0.35f)
    val labelColor = MaterialTheme.appColors.textSecondary
    val areaColor = lineColor.copy(alpha = 0.12f)

    val minVal = points.minOf { it.second }
    val maxVal = points.maxOf { it.second }
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    // Calcular labels del eje Y (valores "bonitos")
    val ySteps = computeNiceYAxisSteps(minVal, maxVal, targetSteps = 4)

    // Calcular labels del eje X (meses)
    val xLabels = selectXAxisLabels(points, compact = rotateXLabels)

    Canvas(modifier = modifier) {
        val leftPad = 56.dp.toPx()
        val rightPad = 12.dp.toPx()
        val topPad = 8.dp.toPx()
        val bottomPad = if (rotateXLabels) 36.dp.toPx() else 32.dp.toPx()
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

        // ── Labels del eje Y ───────────────────────────────────────────
        if (!balancesHidden) {
            val labelStyle = TextStyle(
                fontSize = 9.sp,
                color = labelColor
            )
            ySteps.forEach { value ->
                val yRatio = ((value - minVal) / range).toFloat()
                val y = topPad + chartH * (1f - yRatio)
                val label = formatAxisLabel(value)
                val textResult = textMeasurer.measure(label, labelStyle)
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        leftPad - textResult.size.width - 4.dp.toPx(),
                        y - textResult.size.height / 2f
                    )
                )
            }
        }

        // ── Labels del eje X ───────────────────────────────────────────
        val xLabelStyle = TextStyle(
            fontSize = if (rotateXLabels) 8.sp else 9.sp,
            color = labelColor
        )
        xLabels.forEach { xLabel ->
            val x = leftPad + chartW * xLabel.index / (points.size - 1).toFloat()

            if (rotateXLabels) {
                // ── Modo rotado: formato "MM/YY" a -65° ──────────────
                val labelText = xLabel.compactFormat
                val textResult = textMeasurer.measure(labelText, xLabelStyle)
                val pivotX = x
                val pivotY = topPad + chartH + 4.dp.toPx()
                rotate(degrees = -65f, pivot = Offset(pivotX, pivotY)) {
                    drawText(
                        textLayoutResult = textResult,
                        topLeft = Offset(
                            pivotX - textResult.size.width / 2f,
                            pivotY - textResult.size.height.toFloat()
                        )
                    )
                }
            } else {
                // ── Modo normal (sin rotación) ─────────────────────────
                val labelText = if (xLabel.yearSuffix != null) {
                    "${xLabel.monthAbbr} '${xLabel.yearSuffix}"
                } else {
                    xLabel.monthAbbr
                }
                val textResult = textMeasurer.measure(labelText, xLabelStyle)
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x - textResult.size.width / 2f,
                        topPad + chartH + 6.dp.toPx()
                    )
                )
            }
        }

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
            Icon(Icons.AutoMirrored.Outlined.ShowChart, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.appColors.primary)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Sin datos suficientes",
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Actualiza precios para ver la evolución",
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Funciones de ayuda para los ejes ────────────────────────────────────────

/**
 * Calcula valores "bonitos" (redondos) para el eje Y.
 * Algoritmo: encuentra un step "nice" basado en la magnitud del rango.
 */
private fun computeNiceYAxisSteps(minVal: Double, maxVal: Double, targetSteps: Int): List<Double> {
    val range = maxVal - minVal
    if (range <= 0) {
        // Datos planos: crear steps alrededor del valor
        val center = minVal
        return listOf(center - 1, center, center + 1)
    }

    val roughStep = range / targetSteps
    val magnitude = 10.0.pow(floor(log10(roughStep)).toInt())
    val residual = roughStep / magnitude

    val niceStep = when {
        residual <= 1.5 -> 1.0 * magnitude
        residual <= 3.5 -> 2.0 * magnitude
        residual <= 7.5 -> 5.0 * magnitude
        else -> 10.0 * magnitude
    }

    val niceMin = floor(minVal / niceStep) * niceStep
    val niceMax = ceil(maxVal / niceStep) * niceStep

    val steps = mutableListOf<Double>()
    var current = niceMin
    while (current <= niceMax + niceStep * 0.001) {
        steps.add(current)
        current += niceStep
    }

    // Asegurar que hay al menos 2 steps
    if (steps.size < 2) {
        val center = (minVal + maxVal) / 2
        return listOf(center - niceStep, center, center + niceStep)
    }

    // Limitar a máximo 6 steps
    return if (steps.size > 6) {
        steps.filterIndexed { index, _ -> index % (steps.size / 4) == 0 || index == steps.lastIndex }
    } else {
        steps
    }
}

/**
 * Datos para un label del eje X.
 */
private data class XAxisLabel(
    val index: Int,           // Índice del punto en la lista
    val monthAbbr: String,    // Abreviatura del mes (ene, feb, etc.)
    val yearSuffix: String?,  // Sufijo del año (24, 25...) o null si no mostrar
    val monthNum: Int = 0,    // Número de mes (1-12) para formato compacto
    val yearNum: Int = 0      // Año completo para formato compacto
) {
    /** Formato compacto "MM/YY" para labels rotadas. */
    val compactFormat: String
        get() {
            val mm = monthNum.toString().padStart(2, '0')
            val yy = yearNum.toString().takeLast(2)
            return "$mm/$yy"
        }
}

/**
 * Selecciona qué labels mostrar en el eje X.
 *
 * @param compact Si es true, usa formato compacto "MM/YY" y muestra más marcas
 *                (1 cada 2 meses para datos densos, todas si ≤12 puntos).
 *                Si es false, usa el formato normal "ene '24" con ~4 marcas/año.
 * Siempre incluye el primer y último punto.
 */
private fun selectXAxisLabels(
    points: List<Pair<Long, Double>>,
    compact: Boolean = false
): List<XAxisLabel> {
    if (points.isEmpty()) return emptyList()

    // Construir lista completa de labels potenciales con todos los campos
    val allLabels = points.mapIndexed { idx, (epoch, _) ->
        val instant = Instant.fromEpochMilliseconds(epoch)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        XAxisLabel(
            index = idx,
            monthAbbr = MONTH_ABBR[local.monthNumber - 1],
            yearSuffix = local.year.toString().takeLast(2),
            monthNum = local.monthNumber,
            yearNum = local.year
        )
    }

    return if (compact) selectCompactLabels(allLabels)
    else selectNormalLabels(points, allLabels)
}

/**
 * Modo compacto: formato "MM/YY".
 * - ≤ 12 puntos → mostrar todos
 * - > 12 puntos → mostrar 1 de cada 2 (meses impares: enero, marzo, mayo, etc.)
 * - Siempre incluye el primer y último punto.
 */
private fun selectCompactLabels(allLabels: List<XAxisLabel>): List<XAxisLabel> {
    if (allLabels.size <= 12) return allLabels

    val result = mutableListOf<XAxisLabel>()
    result.add(allLabels.first())

    for (label in allLabels) {
        if (label.index == 0 || label.index == allLabels.lastIndex) continue
        // Meses impares: 1, 3, 5, 7, 9, 11 → ~6 labels/año
        if (label.monthNum % 2 == 1) {
            val last = result.lastOrNull()
            if (last == null || (label.index - last.index) >= 2) {
                result.add(label)
            }
        }
    }

    if (result.lastOrNull()?.index != allLabels.lastIndex) {
        result.add(allLabels.last())
    }

    return result
}

/**
 * Modo normal: formato "ene '24".
 * Muestra ~4 meses por año (enero, abril, julio, octubre).
 */
private fun selectNormalLabels(
    points: List<Pair<Long, Double>>,
    allLabels: List<XAxisLabel>
): List<XAxisLabel> {
    if (points.size <= 6) return allLabels

    val years = points.map {
        Instant.fromEpochMilliseconds(it.first)
            .toLocalDateTime(TimeZone.currentSystemDefault()).year
    }.toSet()
    val showYearSuffix = years.size > 1

    val labels = mutableListOf<XAxisLabel>()

    // Primer punto
    labels.add(allLabels.first().copy(
        yearSuffix = if (showYearSuffix) allLabels.first().yearSuffix else null
    ))

    // Puntos intermedios: meses 1, 4, 7, 10
    points.forEachIndexed { idx, (epoch, _) ->
        if (idx == 0 || idx == points.lastIndex) return@forEachIndexed
        val instant = Instant.fromEpochMilliseconds(epoch)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = local.monthNumber

        val shouldShow = month == 1 || month == 4 || month == 7 || month == 10
        if (shouldShow) {
            val yearSuffix = if (month == 1 && showYearSuffix) {
                local.year.toString().takeLast(2)
            } else if (showYearSuffix) {
                val prevLabel = labels.lastOrNull()
                if (prevLabel?.yearSuffix != null) null else local.year.toString().takeLast(2)
            } else {
                null
            }
            val lastLabel = labels.lastOrNull()
            if (lastLabel == null || (idx - lastLabel.index) >= 2) {
                labels.add(allLabels[idx].copy(yearSuffix = yearSuffix))
            }
        }
    }

    // Último punto
    if (labels.lastOrNull()?.index != points.lastIndex) {
        labels.add(allLabels.last().copy(
            yearSuffix = if (showYearSuffix) allLabels.last().yearSuffix else null
        ))
    }

    return labels
}
