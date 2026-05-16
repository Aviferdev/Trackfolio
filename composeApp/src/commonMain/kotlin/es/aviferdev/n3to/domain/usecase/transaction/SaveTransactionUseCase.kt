package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.repository.TransactionRepository

class SaveTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> = repository.saveTransaction(transaction)
}
