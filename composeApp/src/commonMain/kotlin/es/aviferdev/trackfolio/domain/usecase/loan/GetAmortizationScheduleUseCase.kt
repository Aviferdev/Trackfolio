package es.aviferdev.trackfolio.domain.usecase.loan

import es.aviferdev.trackfolio.domain.loan.FrenchAmortizationCalculator
import es.aviferdev.trackfolio.domain.model.AmortizationEntry
import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.model.LoanRateChange
import es.aviferdev.trackfolio.domain.repository.LoanRateChangeRepository
import es.aviferdev.trackfolio.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetAmortizationScheduleUseCase(
    private val loanRepository: LoanRepository,
    private val rateChangeRepository: LoanRateChangeRepository
) {
    operator fun invoke(loanId: String): Flow<List<AmortizationEntry>> =
        loanRepository.getById(loanId).flatMapLatest { loan ->
            if (loan == null) flowOf(emptyList())
            else rateChangeRepository.getByLoan(loanId).map { rateChanges ->
                FrenchAmortizationCalculator.generateSchedule(
                    totalAmount       = loan.totalAmount,
                    annualRate        = loan.currentInterestRate,
                    totalInstallments = loan.totalInstallments,
                    startDate         = loan.startDate,
                    rateChanges       = rateChanges
                )
            }
        }
}
