package es.aviferdev.n3to.domain.model

/**
 * Inversión neta mensual: ΣBUY − ΣSELL (excluye TRANSFER_IN/OUT).
 *
 * A diferencia de [MonthlyInvestment] (que solo suma compras), este modelo
 * descuenta las ventas para reflejar el flujo de caja neto hacia el portfolio.
 * Así el trading intra-mes (comprar y vender) no infla artificialmente la métrica.
 *
 * @property year      Año en formato "2026".
 * @property month     Mes en formato "01".."12".
 * @property netAmount ΣBUY − ΣSELL en euros.
 */
data class MonthlyNetInvestment(
    val year: String,
    val month: String,
    val netAmount: Double
)
