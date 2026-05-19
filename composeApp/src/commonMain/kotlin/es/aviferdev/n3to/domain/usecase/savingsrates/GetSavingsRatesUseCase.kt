package es.aviferdev.n3to.domain.usecase.savingsrates

import es.aviferdev.n3to.domain.model.SavingsRate
import es.aviferdev.n3to.domain.model.SavingsRateType
import es.aviferdev.n3to.domain.repository.SavingsRatesRepository

class GetSavingsRatesUseCase(
    private val repository: SavingsRatesRepository
) {
    suspend operator fun invoke(
        type: SavingsRateType,
        forceRefresh: Boolean = false
    ): Result<List<SavingsRate>> = when (type) {
        SavingsRateType.SHORT_TERM -> repository.getShortTerm(forceRefresh)
        SavingsRateType.MEDIUM_TERM -> repository.getMediumTerm(forceRefresh)
    }

    fun getLastFetchedAt(type: SavingsRateType): Long? = repository.getLastFetchedAt(type)
}
