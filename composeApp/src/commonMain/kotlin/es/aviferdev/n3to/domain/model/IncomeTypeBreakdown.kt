package es.aviferdev.n3to.domain.model

/**
 * Representa el desglose de ingresos por tipo (IncomeType) para un período determinado.
 * Se usa para el gráfico donut de ingresos en el resumen anual.
 */
data class IncomeTypeBreakdown(
    val incomeType: String,
    val label: String,
    val emoji: String,
    val amount: Double
)