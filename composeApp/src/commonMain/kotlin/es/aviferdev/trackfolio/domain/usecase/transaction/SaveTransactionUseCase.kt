package es.aviferdev.trackfolio.domain.usecase.transaction

import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.repository.TransactionRepository

class SaveTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> = repository.saveTransaction(transaction)
}
