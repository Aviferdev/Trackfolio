package es.aviferdev.trackfolio.ui.annual

import androidx.compose.ui.graphics.Color

/**
 * Representa una porción del gráfico donut.
 * Se usa para gráficos de gastos por categoría e ingresos por tipo.
 */
data class DonutSlice(
    val name: String,
    val icon: String,
    val amount: Double,
    val percent: Double,
    val color: Color
)