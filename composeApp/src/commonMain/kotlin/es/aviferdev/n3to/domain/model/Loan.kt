package es.aviferdev.n3to.domain.model

data class Loan(
    val id: String,
    val accountId: String,
    val name: String,
    val type: LoanType,
    val totalAmount: Double,
    val outstandingPrincipal: Double,
    val currentInterestRate: Double,
    val monthlyPayment: Double,
    val totalInstallments: Int,
    val paidInstallments: Int,
    val startDate: Long,
    val endDate: Long,
    val lenderName: String?,
    val notes: String?,
    val archived: Boolean,
    val createdAt: Long
) {
    val remainingInstallments: Int
        get() = (totalInstallments - paidInstallments).coerceAtLeast(0)

    val progressPercent: Float
        get() = if (totalInstallments > 0)
            (paidInstallments.toFloat() / totalInstallments.toFloat()).coerceIn(0f, 1f)
        else 0f

    val isFullyPaid: Boolean
        get() = paidInstallments >= totalInstallments || outstandingPrincipal <= 0.0

    /** Importe total que se pagará a lo largo de la vida del préstamo. */
    val totalAmountToRepay: Double
        get() = monthlyPayment * totalInstallments

    /** Total de intereses estimados = total a pagar − capital original. */
    val totalEstimatedInterest: Double
        get() = (totalAmountToRepay - totalAmount).coerceAtLeast(0.0)

    /** Capital ya amortizado. */
    val principalPaid: Double
        get() = totalAmount - outstandingPrincipal
}
