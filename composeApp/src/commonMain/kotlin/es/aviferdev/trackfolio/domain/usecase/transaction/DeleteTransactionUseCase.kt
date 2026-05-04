package es.aviferdev.trackfolio.domain.usecase.transaction

import es.aviferdev.trackfolio.domain.repository.TransactionRepository

class DeleteTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteTransaction(id)
}
