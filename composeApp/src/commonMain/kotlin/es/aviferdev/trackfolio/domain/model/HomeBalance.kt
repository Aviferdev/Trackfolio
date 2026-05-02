package es.aviferdev.trackfolio.domain.model

data class HomeBalance(
    val totalCash: Double,
    val netBalance: Double,
    val totalOwed: Double,
    val totalOwing: Double,
    val recentTransactions: List<Transaction>
)
