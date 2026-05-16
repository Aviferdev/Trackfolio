package es.aviferdev.n3to.domain.usecase.debt

import es.aviferdev.n3to.domain.model.Debt
import es.aviferdev.n3to.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow

class GetActiveDebtsUseCase(private val repository: DebtRepository) {
    operator fun invoke(accountId: String): Flow<List<Debt>> =
        repository.getActiveByAccount(accountId)
}
