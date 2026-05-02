package es.aviferdev.trackfolio.domain.model

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val currency: String,
    val balance: Double,
    val createdAt: Long
)

enum class AccountType { CASH, BANK, OTHER }
