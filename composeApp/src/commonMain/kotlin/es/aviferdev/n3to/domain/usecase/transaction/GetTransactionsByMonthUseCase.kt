package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionsByMonthUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String, month: String): Flow<List<Transaction>> =
        repository.getTransactionsByMonthAndAccount(accountId, year, month)
}
