package es.aviferdev.trackfolio.domain.model

/**
 * Representa la inversión mensual (compras de activos) para un mes determinado.
 * Se usa para el gráfico de barras de inversiones en el resumen anual.
 */
data class MonthlyInvestment(
    val year: String,
    val month: String, // "01".."12"
    val amount: Double
)