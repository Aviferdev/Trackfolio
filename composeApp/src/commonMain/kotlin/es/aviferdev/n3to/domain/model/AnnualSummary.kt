package es.aviferdev.n3to.domain.model

data class AnnualSummary(
    val year: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val previousYearIncome: Double,
    val previousYearExpense: Double
) {
    val balance: Double get() = totalIncome - totalExpense

    val incomeVariationPercent: Double
        get() = when {
            previousYearIncome == 0.0 -> 0.0
            else -> ((totalIncome - previousYearIncome) / previousYearIncome) * 100
        }

    val expenseVariationPercent: Double
        get() = when {
            previousYearExpense == 0.0 -> 0.0
            else -> ((totalExpense - previousYearExpense) / previousYearExpense) * 100
        }
}
