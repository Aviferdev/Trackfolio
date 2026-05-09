package es.aviferdev.trackfolio.domain.usecase.loan

import es.aviferdev.trackfolio.domain.loan.FrenchAmortizationCalculator
import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.repository.LoanRepository

class SaveLoanUseCase(
    private val repository: LoanRepository
) {
    suspend operator fun invoke(loan: Loan): Result<Unit> {
        // Calcular cuota mensual con amortización francesa
        val monthlyPayment = FrenchAmortizationCalculator.calculateMonthlyPayment(
            principal  = loan.totalAmount,
            annualRate = loan.currentInterestRate,
            months     = loan.totalInstallments
        )

        val loanWithPayment = loan.copy(
            monthlyPayment       = monthlyPayment,
            outstandingPrincipal = loan.totalAmount
        )

        return repository.insert(loanWithPayment)
    }
}
