package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.AnnualSummary
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetAnnualSummaryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String, year: String): Flow<AnnualSummary> =
        repository.getAnnualSummaryByAccount(accountId, year)
}
