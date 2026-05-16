package es.aviferdev.n3to.domain.model

/**
 * Representa el desglose de gastos por categoría para un período determinado.
 * Se usa para el gráfico donut de gastos en el resumen anual.
 */
data class CategoryBreakdown(
    val categoryId: String?,
    val categoryName: String,
    val amount: Double
)