package es.aviferdev.trackfolio.domain.usecase.realestate

import es.aviferdev.trackfolio.domain.model.RealEstateProperty
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Resumen financiero de una propiedad basado en transacciones del ledger.
 */
data class PropertyFinancialSummary(
    val grossYieldOnPurchase: Double,
    val grossYieldOnCurrent: Double,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netCashflow: Double
)

class GetPropertyFinancialSummaryUseCase(
    private val transactionRepository: TransactionRepository
) {
    /**
     * Calcula el resumen financiero de una propiedad.
     *
     * @param property Propiedad para la que calcular.
     * @param linkedTransactions Lista de transacciones vinculadas (opcional, si no se proporciona se cargan automáticamente).
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

        val annualRent = if (property.isRented && property.monthlyRent != null) {
            property.monthlyRent * 12.0
        } else 0.0

        val grossYieldOnPurchase = if (property.purchaseValue > 0 && annualRent > 0) {
            (annualRent / property.purchaseValue) * 100.0
        } else 0.0

        val grossYieldOnCurrent = if (property.currentEstimatedValue > 0 && annualRent > 0) {
            (annualRent / property.currentEstimatedValue) * 100.0
        } else 0.0

        return PropertyFinancialSummary(
            grossYieldOnPurchase = grossYieldOnPurchase,
            grossYieldOnCurrent  = grossYieldOnCurrent,
            totalIncome          = totalIncome,
            totalExpenses        = totalExpenses,
            netCashflow          = netCashflow
        )
    }
}
