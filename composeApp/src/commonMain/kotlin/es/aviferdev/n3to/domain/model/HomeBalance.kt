package es.aviferdev.n3to.domain.model

data class HomeBalance(
    val selectedAccount: Account?,
    val selectedAccountBalance: Double,
    val totalOwed: Double,
    val totalOwing: Double,
    val recentTransactions: List<Transaction>
)
