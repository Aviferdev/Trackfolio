package es.aviferdev.n3to.domain.usecase.valuable

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.model.ValuableExpense
import es.aviferdev.n3to.domain.model.ValidationError
import es.aviferdev.n3to.domain.repository.TransactionRepository
import es.aviferdev.n3to.domain.repository.ValuableRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Ejecuta la venta de un bien de forma atómica:
 * 1. Actualiza el bien con saleDate y salePrice
 * 2. Crea/actualiza una Transaction INCOME por el precio de venta
 * 3. Crea Transactions EXPENSE por cada gasto de venta
 */
class SellValuableUseCase(
    private val valuableRepository: ValuableRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        valuableId: String,
        saleDate: Long,
        salePrice: Double,
        saleExpenses: List<ValuableExpense> = emptyList(),
        accountId: String,
        valuableName: String
    ): Result<Unit> {
        if (salePrice <= 0) return Result.failure(ValidationError.ValuableSalePriceInvalid)
        if (saleDate <= 0) return Result.failure(ValidationError.ValuableSaleDateRequired)
        saleExpenses.forEach { if (it.amount <= 0) return Result.failure(ValidationError.ValuableExpenseAmountInvalid) }

        // 1. Marcar bien como vendido
        val sellResult = valuableRepository.sell(valuableId, saleDate, salePrice)
        if (sellResult.isFailure) return sellResult

        val now = nowMillis()

        // 2. Transacción de ingreso por venta (crear o actualizar)
        val saleTxId = "val_sell_$valuableId"
        val existingSaleTx = transactionRepository.getTransactionById(saleTxId).firstOrNull()
        val saleTx = Transaction(
            id = saleTxId,
            accountId = accountId,
            amount = salePrice,
            type = TransactionType.INCOME,
            categoryId = null,
            date = saleDate,
            notes = "Venta: $valuableName",
            createdAt = existingSaleTx?.createdAt ?: now,
            linkedValuableId = valuableId
        )
        if (existingSaleTx != null) {
            transactionRepository.updateTransaction(saleTx)
        } else {
            transactionRepository.saveTransaction(saleTx)
        }

        // 3. Eliminar gastos de venta previos (si existían por una edición)
        val existingTransactions =
            transactionRepository.getByLinkedValuable(valuableId).firstOrNull() ?: emptyList()
        existingTransactions
            .filter { it.id.startsWith("val_sexp_$valuableId") }
            .forEach { transactionRepository.deleteTransaction(it.id) }

        // 4. Crear transacciones de gastos de venta
        saleExpenses.forEachIndexed { index, expense ->
            val tx = Transaction(
                id = "val_sexp_${valuableId}_$index",
                accountId = accountId,
                amount = -expense.amount,
                type = TransactionType.EXPENSE,
                categoryId = expense.categoryId,
                date = saleDate,
                notes = expense.notes ?: "Gasto venta: $valuableName",
                createdAt = now,
                linkedValuableId = valuableId
            )
            transactionRepository.saveTransaction(tx)
        }

        return Result.success(Unit)
    }
}
