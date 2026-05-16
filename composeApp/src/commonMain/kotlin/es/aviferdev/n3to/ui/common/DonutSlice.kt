package es.aviferdev.n3to.ui.common

import androidx.compose.ui.graphics.Color

/**
 * Representa una porción de un gráfico donut.
 * Compartido entre Annual, Portfolio y NetWorth.
 */
data class DonutSlice(
    val name: String,
    val icon: String,
    val amount: Double,
    val percent: Double,
    val color: Color
)
