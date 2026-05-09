package es.aviferdev.trackfolio.domain.usecase.loan

import com.benasher44.uuid.uuid4
import es.aviferdev.trackfolio.domain.loan.FrenchAmortizationCalculator
import es.aviferdev.trackfolio.domain.model.LoanRateChange
import es.aviferdev.trackfolio.domain.repository.LoanRepository
import es.aviferdev.trackfolio.domain.repository.LoanRateChangeRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

class UpdateLoanRateUseCase(
    private val loanRepository: LoanRepository,
    private val rateChangeRepository: LoanRateChangeRepository
) {
    suspend operator fun invoke(
        loanId: String,
        newRate: Double,
        effectiveDate: Long
    ): Result<Unit> = runCatching {
        val loan = loanRepository.getById(loanId).first()
            ?: throw IllegalArgumentException("Préstamo no encontrado: $loanId")

        val now = Clock.System.now().toEpochMilliseconds()

        // Registrar el cambio de tipo
        val rateChange = LoanRateChange(
            id            = uuid4().toString(),
            loanId        = loanId,
            newRate       = newRate,
            previousRate  = loan.currentInterestRate,
            effectiveDate = effectiveDate,
            createdAt     = now
        )
        rateChangeRepository.insert(rateChange).getOrThrow()

        // Recalcular cuota con nuevo tipo y cuotas restantes
        val remaining = loan.remainingInstallments
        val newPayment = FrenchAmortizationCalculator.calculateMonthlyPayment(
            principal  = loan.outstandingPrincipal,
            annualRate = newRate,
            months     = remaining
        )

        loanRepository.updateRate(
            id                   = loanId,
            newRate              = newRate,
            newMonthlyPayment    = newPayment,
            outstandingPrincipal = loan.outstandingPrincipal
        ).getOrThrow()
    }
}
