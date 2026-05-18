package es.aviferdev.n3to.domain.usecase.loan

import es.aviferdev.n3to.platform.nowMillis
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.loan.FrenchAmortizationCalculator
import es.aviferdev.n3to.domain.model.LoanRateChange
import es.aviferdev.n3to.domain.repository.LoanRepository
import es.aviferdev.n3to.domain.repository.LoanRateChangeRepository
import kotlinx.coroutines.flow.first

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

        val now = nowMillis()

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
