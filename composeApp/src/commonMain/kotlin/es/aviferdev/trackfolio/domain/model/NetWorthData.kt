package es.aviferdev.trackfolio.domain.model

/**
 * Datos agregados de patrimonio neto.
 *
 * Patrimonio neto = balance de cuentas + valor portfolio − pasivos pendientes.
 */
data class NetWorthData(
    /** Balance total de todas las cuentas (computedBalance). */
    val totalAccountBalance: Double,
    /** Valor total del portfolio (activos × precio actual). */
    val totalPortfolioValue: Double,
    /** Valor total de posiciones de renta fija abiertas. */
    val totalFixedIncomeValue: Double,
    /** Total de capital pendiente de préstamos activos. */
    val totalLoansOutstanding: Double,
    /** Total de deudas cotidianas pendientes (I_OWE). */
    val totalDebtsOwing: Double,
    /** Préstamos activos para mostrar en la sección de pasivos. */
    val loans: List<Loan>
) {
    /** Patrimonio neto = activos − pasivos. */
    val netWorth: Double
        get() = totalAccountBalance + totalPortfolioValue + totalFixedIncomeValue -
                totalLoansOutstanding - totalDebtsOwing

    /** Total de activos = balance + portfolio + renta fija. */
    val totalAssets: Double
        get() = totalAccountBalance + totalPortfolioValue + totalFixedIncomeValue

    /** Total de pasivos = préstamos + deudas cotidianas. */
    val totalLiabilities: Double
        get() = totalLoansOutstanding + totalDebtsOwing
}
