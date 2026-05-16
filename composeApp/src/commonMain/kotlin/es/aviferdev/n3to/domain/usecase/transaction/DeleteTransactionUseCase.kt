package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.repository.TransactionRepository

class DeleteTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteTransaction(id)
}
