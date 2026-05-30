package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.platform.nowMillis
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.PropertyExpense
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionLink
import es.aviferdev.n3to.domain.model.TransactionLinkType
import es.aviferdev.n3to.domain.model.ValidationError
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Ejecuta la venta de una propiedad de forma atómica:
 * 1. Actualiza la propiedad con saleDate, saleValue, archived=true
 * 2. Crea/actualiza una Transaction INCOME por el precio de venta
 * 3. Crea Transactions EXPENSE por cada gasto de venta
 */
class SellPropertyUseCase(
    private val propertyRepository: RealEstatePropertyRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        propertyId: String,
        saleDate: Long,
        saleValue: Double,
        saleExpenses: List<PropertyExpense> = emptyList(),
        accountId: String,
        propertyName: String
    ): Result<Unit> {
        if (saleValue <= 0) return Result.failure(ValidationError.SalePriceInvalid)
        if (saleDate <= 0) return Result.failure(ValidationError.SaleDateRequired)
        saleExpenses.forEach { if (it.amount <= 0) return Result.failure(ValidationError.ExpenseAmountInvalid) }

        // 1. Marcar propiedad como vendida
        val sellResult = propertyRepository.sellProperty(propertyId, saleDate, saleValue)
        if (sellResult.isFailure) return sellResult

        val now = nowMillis()
        val propertyLink = TransactionLink(
            id = "link_prop_sell_$propertyId",
            linkType = TransactionLinkType.PROPERTY,
            linkedEntityId = propertyId
        )

        // 2. Transacción de ingreso por venta (crear o actualizar)
        val saleTxId = "prop_sell_$propertyId"
        val existingSaleTx = transactionRepository.getTransactionById(saleTxId).firstOrNull()
        val saleTx = Transaction(
            id = saleTxId,
            accountId = accountId,
            amount = saleValue,
            type = TransactionType.INCOME,
            categoryId = null,
            date = saleDate,
            notes = "Venta: $propertyName",
            createdAt = existingSaleTx?.createdAt ?: now,
            links = listOf(propertyLink)
        )
        if (existingSaleTx != null) {
            transactionRepository.updateTransaction(saleTx)
        } else {
            transactionRepository.saveTransaction(saleTx)
        }

        // 3. Eliminar gastos de venta previos (si existían por una edición)
        val existingTransactions =
            transactionRepository.getByLinkedProperty(propertyId).firstOrNull() ?: emptyList()
        existingTransactions
            .filter { it.id.startsWith("prop_sexp_$propertyId") }
            .forEach { transactionRepository.deleteTransaction(it.id) }

        // 4. Crear transacciones de gastos de venta
        saleExpenses.forEachIndexed { index, expense ->
            val tx = Transaction(
                id = "prop_sexp_${propertyId}_$index",
                accountId = accountId,
                amount = -expense.amount,
                type = TransactionType.EXPENSE,
                categoryId = expense.categoryId,
                date = saleDate,
                notes = expense.notes ?: "Gasto venta: $propertyName",
                createdAt = now,
                links = listOf(propertyLink)
            )
            transactionRepository.saveTransaction(tx)
        }

        return Result.success(Unit)
    }
}
