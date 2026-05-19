package es.aviferdev.n3to.domain.model

data class SavingsRate(
    val entity: String,
    val interestRate: Double,
    val maxAmount: Double?,
    val minAmount: Double?,
    val termMonths: Int?,
    val conditions: String?,
    val earlyRedemption: Boolean?,
    val country: String?,
    val fgdGuaranteed: Boolean?,
    val obligation720: Boolean?,
    val type: SavingsRateType,
    val fetchedAt: Long
)
