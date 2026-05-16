package es.aviferdev.trackfolio.domain.usecase.realestate

import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsByPropertyUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(propertyId: String): Flow<List<Transaction>> {
        return transactionRepository.getByLinkedProperty(propertyId)
    }
}
