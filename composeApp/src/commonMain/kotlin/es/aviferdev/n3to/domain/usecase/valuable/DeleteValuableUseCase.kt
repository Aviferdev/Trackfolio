package es.aviferdev.n3to.domain.usecase.valuable

import es.aviferdev.n3to.domain.repository.TransactionRepository
import es.aviferdev.n3to.domain.repository.ValuableRepository
import kotlinx.coroutines.flow.firstOrNull

class DeleteValuableUseCase(
    private val valuableRepository: ValuableRepository,
    private val transactionRepository: TransactionRepository
) {
    /**
     * Elimina un bien y todas sus transacciones vinculadas del ledger.
     */
    suspend operator fun invoke(valuableId: String): Result<Unit> {
        // Eliminar transacciones vinculadas
        val linkedTxs =
            transactionRepository.getByLinkedValuable(valuableId).firstOrNull() ?: emptyList()
        linkedTxs.forEach { transactionRepository.deleteTransaction(it.id) }

        // Eliminar el bien
        return valuableRepository.delete(valuableId)
    }
}
