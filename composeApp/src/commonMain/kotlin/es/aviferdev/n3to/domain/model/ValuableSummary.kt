package es.aviferdev.n3to.domain.model

/**
 * Vista agregada de un bien con sus gastos y préstamo vinculado.
 * Proporciona el cálculo completo de beneficio neto, teniendo en cuenta
 * todos los gastos asociados (compra, tenencia y venta).
 *
 * @property valuable El bien.
 * @property purchaseExpenses Suma de gastos de compra.
 * @property holdingExpenses Suma de gastos de tenencia (almacenaje, seguro, mantenimiento...).
 * @property saleExpenses Suma de gastos de venta (comisiones, transporte, marketing...).
 * @property linkedLoan Préstamo vinculado al bien, si existe.
 */
data class ValuableSummary(
    val valuable: Valuable,
    val purchaseExpenses: Double = 0.0,
    val holdingExpenses: Double = 0.0,
    val saleExpenses: Double = 0.0,
    val linkedLoan: Loan? = null
) {
    // ── Propiedades calculadas ──────────────────────────────────────

    /** Suma de todos los gastos asociados al bien. */
    val totalExpenses: Double
        get() = purchaseExpenses + holdingExpenses + saleExpenses

    /** Coste total del bien (compra + gastos de compra). */
    val totalCost: Double
        get() = valuable.purchasePrice + purchaseExpenses

    /** Beneficio neto = precio venta − precio compra − gastos totales.
     *  Null si el bien no se ha vendido. */
    val realizedProfit: Double?
        get() {
            val gross = valuable.grossProfit ?: return null
            return gross - totalExpenses
        }

    /** Rentabilidad neta sobre el coste total (%). */
    val realizedProfitPercent: Double?
        get() {
            val profit = realizedProfit ?: return null
            return if (totalCost > 0) (profit / totalCost) * 100.0 else 0.0
        }
}
