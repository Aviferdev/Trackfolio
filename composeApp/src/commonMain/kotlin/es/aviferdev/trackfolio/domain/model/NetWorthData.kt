package es.aviferdev.trackfolio.domain.model

/**
 * Datos agregados de patrimonio neto.
 * Patrimonio neto = balance de cuentas + valor portfolio + renta fija + inmuebles − pasivos pendientes.
 */
data class NetWorthData(
    val totalAccountBalance: Double,
    val totalPortfolioValue: Double,
    val totalFixedIncomeValue: Double,
    val totalRealEstateValue: Double = 0.0,
    val totalLoansOutstanding: Double,
    val totalDebtsOwing: Double,
    val loans: List<Loan>,
    val properties: List<RealEstateProperty> = emptyList()
) {
    val netWorth: Double
        get() = totalAccountBalance + totalPortfolioValue + totalFixedIncomeValue +
                totalRealEstateValue - totalLoansOutstanding - totalDebtsOwing

    val totalAssets: Double
        get() = totalAccountBalance + totalPortfolioValue + totalFixedIncomeValue +
                totalRealEstateValue

    val totalLiabilities: Double
        get() = totalLoansOutstanding + totalDebtsOwing
}
