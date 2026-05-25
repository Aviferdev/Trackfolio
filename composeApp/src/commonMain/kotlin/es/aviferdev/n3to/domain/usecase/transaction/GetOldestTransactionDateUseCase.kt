package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetOldestTransactionDateUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String): Flow<Long?> =
        repository.getOldestDate(accountId)
}
