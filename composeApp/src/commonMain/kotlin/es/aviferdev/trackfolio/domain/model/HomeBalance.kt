package es.aviferdev.trackfolio.domain.model

data class HomeBalance(
    val selectedAccount: Account?,
    val selectedAccountBalance: Double,
    val totalOwed: Double,
    val totalOwing: Double,
    val recentTransactions: List<Transaction>
)
