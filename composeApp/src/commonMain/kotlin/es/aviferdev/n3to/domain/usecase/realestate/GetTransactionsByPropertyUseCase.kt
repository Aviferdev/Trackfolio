package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsByPropertyUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(propertyId: String): Flow<List<Transaction>> {
        return transactionRepository.getByLinkedProperty(propertyId)
    }
}
