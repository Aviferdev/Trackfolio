package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.InterestFrequency
import es.aviferdev.n3to.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.n3to.domain.portfolio.ScheduledCoupon

class GetCouponScheduleUseCase {
    operator fun invoke(
        position: FixedIncomePosition,
        asOfDate: Long = nowMillis()
    ): List<ScheduledCoupon> {
        if (position.interestFrequency == InterestFrequency.AT_MATURITY) {
            return listOf(
                ScheduledCoupon(
                    date = position.maturityDate,
                    grossAmount = position.estimatedGrossInterest,
                    isPaid = false
                )
            )
        }

        val grossPerCoupon = when (position.interestFrequency) {
            InterestFrequency.MONTHLY -> position.estimatedGrossInterest / (position.totalTermDays / 30.0)
            InterestFrequency.QUARTERLY -> position.estimatedGrossInterest / (position.totalTermDays / 91.0)
            InterestFrequency.SEMIANNUAL -> position.estimatedGrossInterest / (position.totalTermDays / 182.0)
            InterestFrequency.ANNUAL -> position.estimatedGrossInterest / (position.totalTermDays / 365.0)
        }

        return FixedIncomeCalculator.couponSchedule(
            startDate = position.startDate,
            maturityDate = position.maturityDate,
            frequency = position.interestFrequency,
            grossPerCoupon = grossPerCoupon
        )
    }
}