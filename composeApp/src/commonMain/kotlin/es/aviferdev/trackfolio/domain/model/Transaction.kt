package es.aviferdev.trackfolio.domain.model

data class Transaction(
    val id: String,
    val accountId: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val date: Long,
    val notes: String?,
    val createdAt: Long
)

enum class TransactionType { INCOME, EXPENSE }
