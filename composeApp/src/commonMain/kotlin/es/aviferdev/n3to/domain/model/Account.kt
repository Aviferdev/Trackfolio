package es.aviferdev.n3to.domain.model

data class Account(
    val id: String,
    val name: String,
    val initialBalance: Double,   // saldo inicial — obligatorio antes de operar
    val computedBalance: Double,  // initialBalance + SUM(INCOME) - SUM(EXPENSE) + SUM(ADJUSTMENT), calculado en BD
    val createdAt: Long,
    val accountType: AccountType = AccountType.GENERAL
) {
    /** True si el usuario todavía no ha configurado el saldo inicial */
    val needsInitialBalance: Boolean get() = initialBalance == 0.0 && computedBalance == 0.0

    /** True si la cuenta es de efectivo y soporta reconciliación */
    val isCash: Boolean get() = accountType == AccountType.CASH
}

enum class AccountType {
    GENERAL,
    CASH;

    companion object {
        fun fromName(name: String?): AccountType =
            entries.firstOrNull { it.name == name } ?: GENERAL
    }
}
