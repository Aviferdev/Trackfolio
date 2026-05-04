package es.aviferdev.trackfolio.domain.usecase.debt

import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow

class GetActiveDebtsUseCase(private val repository: DebtRepository) {
    operator fun invoke(accountId: String): Flow<List<Debt>> =
        repository.getActiveByAccount(accountId)
}
