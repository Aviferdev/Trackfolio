package es.aviferdev.trackfolio.domain.usecase.transaction

import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsByMonthUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String, month: String): Flow<List<Transaction>> =
        repository.getTransactionsByMonthAndAccount(accountId, year, month)
}
