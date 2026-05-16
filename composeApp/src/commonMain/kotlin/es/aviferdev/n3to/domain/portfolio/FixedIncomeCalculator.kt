package es.aviferdev.n3to.domain.portfolio

import es.aviferdev.n3to.domain.model.*
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

object FixedIncomeCalculator {

    fun calculatePosition(
        position: FixedIncomePosition,
        events: List<FixedIncomeEvent>,
        nowMillis: Long = Clock.System.now().toEpochMilliseconds()
    ): FixedIncomeRow {
        val collectedInterest = events
            .filter { it.type == FixedIncomeEventType.COUPON || it.type == FixedIncomeEventType.MATURITY_SETTLEMENT }
            .sumOf { it.netAmount }

        val currentValue = position.principal + position.accruedInterestToDate

        val commissions = events.sumOf { it.commissionAmount }
        val totalProfit = collectedInterest + position.accruedInterestToDate - commissions

        val totalProfitPercent = if (position.principal > 0.0) {
            (totalProfit / position.principal) * 100.0
        } else 0.0

        return FixedIncomeRow(
            position = position,
            collectedInterest = collectedInterest,
            currentValue = currentValue,
            totalProfit = totalProfit,
            totalProfitPercent = totalProfitPercent
        )
    }

    fun calculateSummary(
        positions: List<FixedIncomePosition>,
        eventsByPosition: Map<String, List<FixedIncomeEvent>>,
        nowMillis: Long = Clock.System.now().toEpochMilliseconds()
    ): FixedIncomeSummary {
        val rows = positions.map { pos ->
            val events = eventsByPosition[pos.id] ?: emptyList()
            calculatePosition(pos, events, nowMillis)
        }

        // Separar posiciones abiertas y cerradas
        val openPositions = positions.filter { it.isOpen }
        val closedPositions = positions.filter { !it.isOpen }

        val openRows = rows.filter { it.position.isOpen }
        val closedRows = rows.filter { !it.position.isOpen }

        val totalPrincipal = openPositions.sumOf { it.principal }
        val totalCurrentValue = openRows.sumOf { it.currentValue }
        val totalAccruedInterest = openPositions.sumOf { it.accruedInterestToDate }
        val totalCollectedInterest = openRows.sumOf { it.collectedInterest }
        val totalNetProfit = openRows.sumOf { it.totalProfit }

        val totalNetProfitPercent = if (totalPrincipal > 0.0) {
            (totalNetProfit / totalPrincipal) * 100.0
        } else 0.0

        val openPositionsCount = openPositions.size
        val nearMaturityCount = openPositions.count { it.isNearMaturity }

        return FixedIncomeSummary(
            totalPrincipal = totalPrincipal,
            totalCurrentValue = totalCurrentValue,
            totalAccruedInterest = totalAccruedInterest,
            totalCollectedInterest = totalCollectedInterest,
            totalNetProfit = totalNetProfit,
            totalNetProfitPercent = totalNetProfitPercent,
            openPositionsCount = openPositionsCount,
            nearMaturityCount = nearMaturityCount,
            positions = openRows,
            closedPositions = closedRows
        )
    }

    fun accruedInterest(
        principal: Double,
        interestRate: Double,
        startDate: Long,
        maturityDate: Long,
        asOfDate: Long
    ): Double {
        val start = Instant.fromEpochMilliseconds(startDate)
        val end = Instant.fromEpochMilliseconds(maturityDate)
        val asOfMillis = asOfDate.coerceIn(startDate, end.toEpochMilliseconds())
        val asOf = Instant.fromEpochMilliseconds(asOfMillis)
        val tz = TimeZone.currentSystemDefault()
        val totalDays = start.daysUntil(end, tz)
        val safeTotal = if (totalDays > 0) totalDays.toDouble() else 1.0
        val elapsedDays = start.daysUntil(asOf, tz)
        val safeElapsed = if (elapsedDays > 0) elapsedDays.toDouble() else 0.0
        val totalInterest = principal * interestRate / 100.0
        return totalInterest * (safeElapsed / safeTotal)
    }

    fun nextCouponDate(
        startDate: Long,
        maturityDate: Long,
        frequency: InterestFrequency,
        asOfDate: Long
    ): Long? {
        if (frequency == InterestFrequency.AT_MATURITY) return null

        val intervalDays = when (frequency) {
            InterestFrequency.MONTHLY -> 30
            InterestFrequency.QUARTERLY -> 91
            InterestFrequency.SEMIANNUAL -> 182
            InterestFrequency.ANNUAL -> 365
            else -> return null
        }

        var nextDate = startDate
        val maturity = Instant.fromEpochMilliseconds(maturityDate)
        while (Instant.fromEpochMilliseconds(nextDate) < Instant.fromEpochMilliseconds(asOfDate.coerceAtLeast(startDate))) {
            nextDate += intervalDays * 24 * 60 * 60 * 1000L
        }

        return if (Instant.fromEpochMilliseconds(nextDate) <= maturity) nextDate else null
    }

    fun couponSchedule(
        startDate: Long,
        maturityDate: Long,
        frequency: InterestFrequency,
        grossPerCoupon: Double
    ): List<ScheduledCoupon> {
        if (frequency == InterestFrequency.AT_MATURITY) {
            return listOf(
                ScheduledCoupon(
                    date = maturityDate,
                    grossAmount = grossPerCoupon,
                    isPaid = false
                )
            )
        }

        val intervalDays = when (frequency) {
            InterestFrequency.MONTHLY -> 30
            InterestFrequency.QUARTERLY -> 91
            InterestFrequency.SEMIANNUAL -> 182
            InterestFrequency.ANNUAL -> 365
            else -> return emptyList()
        }

        val schedule = mutableListOf<ScheduledCoupon>()
        var couponDate = startDate
        val maturity = Instant.fromEpochMilliseconds(maturityDate)
        while (Instant.fromEpochMilliseconds(couponDate) < maturity) {
            schedule.add(
                ScheduledCoupon(
                    date = couponDate,
                    grossAmount = grossPerCoupon,
                    isPaid = false
                )
            )
            couponDate += intervalDays * 24 * 60 * 60 * 1000L
        }

        val finalDate = schedule.lastOrNull()?.date ?: maturityDate
        if (finalDate < maturityDate) {
            schedule.add(
                ScheduledCoupon(
                    date = maturityDate,
                    grossAmount = grossPerCoupon,
                    isPaid = false
                )
            )
        }

        return schedule
    }

    fun simulateMaturity(
        position: FixedIncomePosition,
        events: List<FixedIncomeEvent>,
        estimatedIrpfPercent: Double = 19.0,
        estimatedCommission: Double = 0.0
    ): MaturitySimulation {
        val grossInterest = position.estimatedGrossInterest
        val collectedCoupons = events
            .filter { it.type == FixedIncomeEventType.COUPON }
            .sumOf { it.grossAmount }
        val remainingInterest = (grossInterest - collectedCoupons).coerceAtLeast(0.0)

        val irpf = remainingInterest * estimatedIrpfPercent / 100.0
        val netAtMaturity = position.principal + remainingInterest - irpf - estimatedCommission

        return MaturitySimulation(
            capitalInvested = position.principal,
            grossInterest = grossInterest,
            collectedCoupons = collectedCoupons,
            remainingInterest = remainingInterest,
            estimatedIrpf = irpf,
            estimatedCommission = estimatedCommission,
            netAtMaturity = netAtMaturity,
            netProfit = netAtMaturity - position.principal
        )
    }
}

data class ScheduledCoupon(
    val date: Long,
    val grossAmount: Double,
    val isPaid: Boolean
)

data class MaturitySimulation(
    val capitalInvested: Double,
    val grossInterest: Double,
    val collectedCoupons: Double,
    val remainingInterest: Double,
    val estimatedIrpf: Double,
    val estimatedCommission: Double,
    val netAtMaturity: Double,
    val netProfit: Double
)