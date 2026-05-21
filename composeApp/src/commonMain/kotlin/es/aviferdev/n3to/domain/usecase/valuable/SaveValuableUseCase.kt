package es.aviferdev.n3to.domain.usecase.valuable

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.model.ValuableExpense
import es.aviferdev.n3to.domain.model.ValidationError
import es.aviferdev.n3to.domain.repository.TransactionRepository
import es.aviferdev.n3to.domain.repository.ValuableRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Guarda/actualiza un bien y sincroniza automáticamente:
 * - Transacción de compra (EXPENSE) por el precio de compra
 * - Transacciones de gastos de compra (cada una con su categoría)
 * - Transacciones de gastos de tenencia (almacenaje, seguro, etc.)
 *
 * En edición, se eliminan los gastos previos y se recrean.
 */
class SaveValuableUseCase(
    private val valuableRepository: ValuableRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        valuable: Valuable,
        purchaseExpenses: List<ValuableExpense> = emptyList(),
        holdingExpenses: List<ValuableExpense> = emptyList()
    ): Result<Unit> {
        // ── Validaciones ────────────────────────────────────────────────
        if (valuable.name.isNotBlank().not())
            return Result.failure(ValidationError.ValuableNameEmpty)
        if (valuable.purchasePrice <= 0)
            return Result.failure(ValidationError.ValuablePurchasePriceInvalid)
        if (valuable.purchaseDate <= 0)
            return Result.failure(ValidationError.ValuablePurchaseDateRequired)
        purchaseExpenses.forEach { if (it.amount <= 0) return Result.failure(ValidationError.ValuableExpenseAmountInvalid) }
        holdingExpenses.forEach { if (it.amount <= 0) return Result.failure(ValidationError.ValuableExpenseAmountInvalid) }

        // 1. Guardar/actualizar bien
        val saveResult = valuableRepository.save(valuable)
        if (saveResult.isFailure) return saveResult

        val now = nowMillis()

        // 2. Sincronizar transacción de compra (crear o actualizar)
        val buyTxId = "val_buy_${valuable.id}"
        val existingBuyTx = transactionRepository.getTransactionById(buyTxId).firstOrNull()
        val buyTx = Transaction(
            id = buyTxId,
            accountId = valuable.accountId,
            amount = -valuable.purchasePrice,
            type = TransactionType.EXPENSE,
            categoryId = null,
            date = valuable.purchaseDate,
            notes = "Compra: ${valuable.name}",
            createdAt = existingBuyTx?.createdAt ?: now,
            linkedValuableId = valuable.id
        )
        if (existingBuyTx != null) {
            transactionRepository.updateTransaction(buyTx)
        } else {
            transactionRepository.saveTransaction(buyTx)
        }

        // 3. Eliminar gastos previos (compra y tenencia) para evitar duplicados en edición
        val linkedTransactions =
            transactionRepository.getByLinkedValuable(valuable.id).firstOrNull() ?: emptyList()
        linkedTransactions
            .filter { it.id.startsWith("val_pexp_${valuable.id}") || it.id.startsWith("val_hexp_${valuable.id}") }
            .forEach { transactionRepository.deleteTransaction(it.id) }

        // 4. Crear transacciones de gastos de compra
        purchaseExpenses.forEachIndexed { index, expense ->
            val tx = Transaction(
                id = "val_pexp_${valuable.id}_$index",
                accountId = valuable.accountId,
                amount = -expense.amount,
                type = TransactionType.EXPENSE,
                categoryId = expense.categoryId,
                date = valuable.purchaseDate,
                notes = expense.notes ?: "Gasto compra: ${valuable.name}",
                createdAt = now,
                linkedValuableId = valuable.id
            )
            transactionRepository.saveTransaction(tx)
        }

        // 5. Crear transacciones de gastos de tenencia
        holdingExpenses.forEachIndexed { index, expense ->
            val tx = Transaction(
                id = "val_hexp_${valuable.id}_$index",
                accountId = valuable.accountId,
                amount = -expense.amount,
                type = TransactionType.EXPENSE,
                categoryId = expense.categoryId,
                date = valuable.purchaseDate,
                notes = expense.notes ?: "Gasto tenencia: ${valuable.name}",
                createdAt = now,
                linkedValuableId = valuable.id
            )
            transactionRepository.saveTransaction(tx)
        }

        return Result.success(Unit)
    }
}
