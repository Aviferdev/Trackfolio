package es.aviferdev.trackfolio.domain.usecase.transaction

import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionByIdUseCase(private val repository: TransactionRepository) {
    operator fun invoke(id: String): Flow<Transaction?> = repository.getTransactionById(id)
}
