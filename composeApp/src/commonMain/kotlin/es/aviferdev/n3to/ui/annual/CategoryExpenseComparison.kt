package es.aviferdev.n3to.ui.annual

import androidx.compose.ui.graphics.Color

/**
 * Modelo UI para la comparativa interanual de una categoría de gasto
 * o tipo de ingreso en la pantalla de resumen anual.
 */
data class CategoryExpenseComparison(
    val name: String,
    val icon: String,
    val currentAmount: Double,
    val currentPercent: Double,
    val previousAmount: Double?,
    val changePercent: Double?,
    val color: Color
)
