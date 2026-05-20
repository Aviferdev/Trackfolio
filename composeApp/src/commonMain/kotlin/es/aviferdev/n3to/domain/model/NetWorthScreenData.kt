package es.aviferdev.n3to.domain.model


data class NetWorthScreenData(
    val totalAccountBalance: Double,
    val totalPortfolioValue: Double,
    val totalFixedIncomeValue: Double,
    val totalRealEstateValue: Double = 0.0,
    val totalValuablesValue: Double = 0.0,
    val totalLoansOutstanding: Double,
    val totalDebtsOwing: Double,
    val loans: List<Loan>,
    val properties: List<RealEstateProperty> = emptyList(),
    val valuables: List<Valuable> = emptyList()
) {
    val netWorth: Double
        get() = totalAccountBalance + totalPortfolioValue + totalFixedIncomeValue +
                totalRealEstateValue + totalValuablesValue -
                totalLoansOutstanding - totalDebtsOwing

    val totalAssets: Double
        get() = totalAccountBalance + totalPortfolioValue + totalFixedIncomeValue +
                totalRealEstateValue + totalValuablesValue

    val totalLiabilities: Double
        get() = totalLoansOutstanding + totalDebtsOwing
}
