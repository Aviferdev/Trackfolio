package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionByIdUseCase(private val repository: TransactionRepository) {
    operator fun invoke(id: String): Flow<Transaction?> = repository.getTransactionById(id)
}
