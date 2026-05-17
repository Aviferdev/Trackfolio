package es.aviferdev.n3to.domain.usecase.realestate

import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.PropertyExpense
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock

/**
 * Guarda/actualiza una propiedad y sincroniza automáticamente:
 * - Transacción de compra (EXPENSE) por el valor de compra
 * - Transacciones de gastos de compra (cada una con su categoría)
 *
 * En edición, se eliminan los gastos previos y se recrean.
 */
class SavePropertyUseCase(
    private val repository: RealEstatePropertyRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        property: RealEstateProperty,
        purchaseExpenses: List<PropertyExpense> = emptyList()
    ): Result<Unit> {
        require(property.ownershipPercentage in 0.0..100.0) {
            "El porcentaje de propiedad debe estar entre 0 y 100"
        }
        require(property.name.isNotBlank()) { "El nombre no puede estar vacío" }
        require(property.address.isNotBlank()) { "La dirección no puede estar vacía" }
        require(property.purchaseValue > 0) { "El valor de compra debe ser mayor que 0" }
        require(property.currentEstimatedValue > 0) { "El valor estimado debe ser mayor que 0" }
        require(property.acquisitionDate > 0) { "La fecha de adquisición es obligatoria" }
        purchaseExpenses.forEach { require(it.amount > 0) { "El importe del gasto debe ser mayor que 0" } }

        if (property.rentalStatus == RentalStatus.RENTED) {
            require(property.monthlyRent != null && property.monthlyRent > 0) {
                "La renta mensual es obligatoria para propiedades alquiladas"
            }
        }

        // 1. Guardar/actualizar propiedad
        val saveResult = repository.saveProperty(property)
        if (saveResult.isFailure) return saveResult

        val now = Clock.System.now().toEpochMilliseconds()

        // 2. Sincronizar transacción de compra (crear o actualizar)
        val buyTxId = "prop_buy_${property.id}"
        val existingBuyTx = transactionRepository.getTransactionById(buyTxId).firstOrNull()
        val buyTx = Transaction(
            id = buyTxId,
            accountId = property.accountId,
            amount = -property.purchaseValue,
            type = TransactionType.EXPENSE,
            categoryId = null,
            date = property.acquisitionDate,
            notes = "Compra: ${property.name}",
            createdAt = existingBuyTx?.createdAt ?: now,
            linkedPropertyId = property.id
        )
        if (existingBuyTx != null) {
            transactionRepository.updateTransaction(buyTx)
        } else {
            transactionRepository.saveTransaction(buyTx)
        }

        // 3. Eliminar gastos de compra previos (para evitar duplicados en edición)
        val linkedTransactions = transactionRepository.getByLinkedProperty(property.id).firstOrNull() ?: emptyList()
        linkedTransactions
            .filter { it.id.startsWith("prop_pexp_${property.id}") }
            .forEach { transactionRepository.deleteTransaction(it.id) }

        // 4. Crear transacciones de gastos de compra
        purchaseExpenses.forEachIndexed { index, expense ->
            val tx = Transaction(
                id = "prop_pexp_${property.id}_$index",
                accountId = property.accountId,
                amount = -expense.amount,
                type = TransactionType.EXPENSE,
                categoryId = expense.categoryId,
                date = property.acquisitionDate,
                notes = expense.notes ?: "Gasto compra: ${property.name}",
                createdAt = now,
                linkedPropertyId = property.id
            )
            transactionRepository.saveTransaction(tx)
        }

        return Result.success(Unit)
    }
}
