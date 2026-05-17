package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Resumen financiero de una propiedad basado en transacciones del ledger.
 */
data class PropertyFinancialSummary(
    val grossYieldOnPurchase: Double,
    val grossYieldOnCurrent: Double,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netCashflow: Double,
    val totalPurchaseExpenses: Double = 0.0,
    val totalSaleExpenses: Double = 0.0,
    val totalReturn: Double? = null,
    val totalReturnPercent: Double? = null
)

class GetPropertyFinancialSummaryUseCase(
    private val transactionRepository: TransactionRepository
) {
    /**
     * Calcula el resumen financiero de una propiedad.
     *
     * @param property Propiedad para la que calcular.
     * @param linkedTransactions Lista de transacciones vinculadas (opcional).
     */
    suspend operator fun invoke(
        property: RealEstateProperty,
        linkedTransactions: List<Transaction>? = null
    ): PropertyFinancialSummary {
        val transactions = linkedTransactions
            ?: transactionRepository.getByLinkedProperty(property.id).firstOrNull()
            ?: emptyList()

        val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.amount < 0 }.sumOf { -it.amount }
        val netCashflow = totalIncome - totalExpenses

        // Identificar gastos de compra/venta por prefijo de ID
        val totalPurchaseExpenses = transactions
            .filter { it.id.startsWith("prop_pexp_${property.id}") }
            .sumOf { -it.amount }

        val totalSaleExpenses = transactions
            .filter { it.id.startsWith("prop_sexp_${property.id}") }
            .sumOf { -it.amount }

        // Retorno total (solo si vendida)
        val totalReturn = if (property.isSold) {
            totalIncome - totalExpenses
        } else null

        val totalReturnPercent = if (totalReturn != null && property.purchaseValue > 0) {
            (totalReturn / property.purchaseValue) * 100.0
        } else null

        val annualRent = if (property.isRented && property.monthlyRent != null) {
            property.monthlyRent * 12.0
        } else 0.0

        return PropertyFinancialSummary(
            grossYieldOnPurchase  = if (property.purchaseValue > 0 && annualRent > 0) (annualRent / property.purchaseValue) * 100.0 else 0.0,
            grossYieldOnCurrent   = if (property.currentEstimatedValue > 0 && annualRent > 0) (annualRent / property.currentEstimatedValue) * 100.0 else 0.0,
            totalIncome           = totalIncome,
            totalExpenses         = totalExpenses,
            netCashflow           = netCashflow,
            totalPurchaseExpenses = totalPurchaseExpenses,
            totalSaleExpenses     = totalSaleExpenses,
            totalReturn           = totalReturn,
            totalReturnPercent    = totalReturnPercent
        )
    }
}
