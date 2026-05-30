package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.platform.nowMillis
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.PropertyExpense
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.domain.model.ValidationError
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionLink
import es.aviferdev.n3to.domain.model.TransactionLinkType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull

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
        if (property.ownershipPercentage !in 0.0..100.0)
            return Result.failure(ValidationError.OwnershipPercentageInvalid)
        if (property.name.isNotBlank().not())
            return Result.failure(ValidationError.PropertyNameEmpty)
        if (property.address.isNotBlank().not())
            return Result.failure(ValidationError.PropertyAddressEmpty)
        if (property.purchaseValue <= 0)
            return Result.failure(ValidationError.PurchaseValueInvalid)
        if (property.currentEstimatedValue <= 0)
            return Result.failure(ValidationError.EstimatedValueInvalid)
        if (property.acquisitionDate <= 0)
            return Result.failure(ValidationError.AcquisitionDateRequired)
        purchaseExpenses.forEach { if (it.amount <= 0) return Result.failure(ValidationError.ExpenseAmountInvalid) }

        if (property.rentalStatus == RentalStatus.RENTED) {
            if (property.monthlyRent == null || property.monthlyRent <= 0)
                return Result.failure(ValidationError.RentalIncomeRequired)
        }

        // 1. Guardar/actualizar propiedad
        val saveResult = repository.saveProperty(property)
        if (saveResult.isFailure) return saveResult

        val now = nowMillis()
        val propertyLink = TransactionLink(
            id = "link_prop_${property.id}",
            linkType = TransactionLinkType.PROPERTY,
            linkedEntityId = property.id
        )

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
            links = listOf(propertyLink)
        )
        if (existingBuyTx != null) {
            transactionRepository.updateTransaction(buyTx)
        } else {
            transactionRepository.saveTransaction(buyTx)
        }

        // 3. Eliminar gastos de compra previos (para evitar duplicados en edición)
        val linkedTransactions =
            transactionRepository.getByLinkedProperty(property.id).firstOrNull() ?: emptyList()
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
                links = listOf(propertyLink)
            )
            transactionRepository.saveTransaction(tx)
        }

        return Result.success(Unit)
    }
}
