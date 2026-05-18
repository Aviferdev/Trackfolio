package es.aviferdev.n3to.domain.model

import es.aviferdev.n3to.platform.nowMillis

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.daysUntil

data class FixedIncomePosition(
    val id: String,
    val accountId: String,
    val portfolioId: String? = null,
    val assetCategoryId: String? = null,
    val name: String,
    val ticker: String,
    val type: FixedIncomeType,
    val notes: String?,
    val principal: Double,
    val quantity: Double,
    val nominalPerUnit: Double,
    val interestRate: Double,
    val interestFrequency: InterestFrequency,
    val startDate: Long,
    val maturityDate: Long,
    val platformId: String,
    val issuerId: String?,
    val region: String? = null,     // Región para distribución (Europa, EE.UU., Emergentes, etc.)
    val sector: String? = null,      // Sector para distribución (Gobierno, Corporativo, etc.)
    val autoRenew: Boolean,
    val archived: Boolean,
    val closedAt: Long?,
    val closeType: FixedIncomeCloseType?,
    val feeNote: String?,
    val createdAt: Long
) {
    val isOpen: Boolean get() = closedAt == null

    val isBond: Boolean get() = type == FixedIncomeType.BOND
            || type == FixedIncomeType.CORPORATE_BOND
            || type == FixedIncomeType.GOVERNMENT_OBLIGATION
    val isBill: Boolean get() = type == FixedIncomeType.BILL
    val isDeposit: Boolean get() = type == FixedIncomeType.DEPOSIT

    // Indica si la posición tiene cupones periódicos (no AT_MATURITY)
    val hasPeriodicCoupons: Boolean get() = interestFrequency != InterestFrequency.AT_MATURITY

    // Indica si permite venta en mercado secundario
    val allowsSecondarySale: Boolean get() = type.allowsSecondarySale

    // Indica si permite cancelación anticipada
    val allowsEarlyCancellation: Boolean get() = type.allowsEarlyCancellation

    val totalTermDays: Int
        get() {
            val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
            val start = Instant.fromEpochMilliseconds(startDate)
            val end = Instant.fromEpochMilliseconds(maturityDate)
            return start.daysUntil(end, tz)
        }

    val elapsedDays: Int
        get() {
            val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
            val now = nowMillis()
            val endDate = closedAt ?: now
            val start = Instant.fromEpochMilliseconds(startDate)
            val end = Instant.fromEpochMilliseconds(endDate.coerceAtMost(nowMillis()))
            return start.daysUntil(end, tz)
        }

    val remainingDays: Int
        get() {
            val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
            val now = nowMillis()
            return if (closedAt != null || now >= maturityDate) 0
            else {
                val start = Instant.fromEpochMilliseconds(now)
                val end = Instant.fromEpochMilliseconds(maturityDate)
                start.daysUntil(end, tz)
            }.coerceAtLeast(0)
        }

    val progressPercent: Float
        get() = if (totalTermDays > 0) (elapsedDays.toFloat() / totalTermDays.toFloat()).coerceIn(0f, 1f) else 0f

    val isMatured: Boolean get() = nowMillis() >= maturityDate

    val isNearMaturity: Boolean get() = remainingDays in 1..30

    val estimatedGrossInterest: Double get() = principal * interestRate / 100.0

    val accruedInterestToDate: Double
        get() {
            if (totalTermDays <= 0) return 0.0
            return estimatedGrossInterest * (elapsedDays.toDouble() / totalTermDays.toDouble())
        }

    val currentValue: Double get() = principal + accruedInterestToDate

    fun daysUntilNextCoupon(asOfDateMillis: Long = nowMillis()): Int? {
        if (interestFrequency == InterestFrequency.AT_MATURITY) return null

        val intervalDays = when (interestFrequency) {
            InterestFrequency.MONTHLY -> 30
            InterestFrequency.QUARTERLY -> 91
            InterestFrequency.SEMIANNUAL -> 182
            InterestFrequency.ANNUAL -> 365
            else -> return null
        }

        var nextDate = startDate
        while (nextDate <= asOfDateMillis) {
            nextDate += intervalDays * 24 * 60 * 60 * 1000L
        }
        val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
        val start = Instant.fromEpochMilliseconds(asOfDateMillis)
        val end = Instant.fromEpochMilliseconds(nextDate)
        return start.daysUntil(end, tz)
    }
}