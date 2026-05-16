package es.aviferdev.n3to.domain.model

data class MonthlyTotals(
    val year: String,
    val month: String,
    val totalIncome: Double,
    val totalExpense: Double
) {
    val balance: Double get() = totalIncome - totalExpense
}
