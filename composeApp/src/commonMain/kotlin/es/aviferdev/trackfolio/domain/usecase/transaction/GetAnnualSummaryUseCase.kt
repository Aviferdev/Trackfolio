package es.aviferdev.trackfolio.domain.usecase.transaction

import es.aviferdev.trackfolio.domain.model.AnnualSummary
import es.aviferdev.trackfolio.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetAnnualSummaryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(year: String): Flow<AnnualSummary> = repository.getAnnualSummary(year)
}
