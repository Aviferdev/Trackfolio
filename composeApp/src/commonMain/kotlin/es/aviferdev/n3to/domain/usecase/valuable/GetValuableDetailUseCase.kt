package es.aviferdev.n3to.domain.usecase.valuable

import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.model.ValuableSummary
import es.aviferdev.n3to.domain.repository.LoanRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import es.aviferdev.n3to.domain.repository.ValuableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * Obtiene el detalle completo de un bien, incluyendo:
 * - Gastos de compra, tenencia y venta (del ledger)
 * - Préstamo vinculado (si existe)
 * - Beneficio calculado (ValuableSummary)
 */
class GetValuableDetailUseCase(
    private val valuableRepository: ValuableRepository,
    private val transactionRepository: TransactionRepository,
    private val loanRepository: LoanRepository
) {
    operator fun invoke(valuableId: String): Flow<ValuableSummary> =
        valuableRepository.getById(valuableId).map { valuable ->
            if (valuable == null) return@map ValuableSummary(
                valuable = Valuable(
                    id = "",
                    accountId = "",
                    name = "",
                    purchasePrice = 0.0,
                    purchaseDate = 0L,
                    createdAt = 0L
                )
            )

            // Obtener transacciones vinculadas
            val transactions = transactionRepository.getByLinkedValuable(valuableId)
                .firstOrNull() ?: emptyList()

            // Clasificar gastos por prefijo de ID
            val purchaseExpenses = transactions
                .filter { it.id.startsWith("val_pexp_${valuable.id}") }
                .sumOf { -it.amount }

            val holdingExpenses = transactions
                .filter { it.id.startsWith("val_hexp_${valuable.id}") }
                .sumOf { -it.amount }

            val saleExpenses = transactions
                .filter { it.id.startsWith("val_sexp_${valuable.id}") }
                .sumOf { -it.amount }

            // Obtener préstamo vinculado si existe
            val linkedLoan = valuable.linkedLoanId?.let { loanId ->
                loanRepository.getById(loanId).firstOrNull()
            }

            ValuableSummary(
                valuable = valuable,
                purchaseExpenses = purchaseExpenses,
                holdingExpenses = holdingExpenses,
                saleExpenses = saleExpenses,
                linkedLoan = linkedLoan
            )
        }
}
