package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.domain.model.FixedIncomeSummary
import es.aviferdev.n3to.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFixedIncomeSummaryUseCase(
    private val positionRepository: FixedIncomeRepository,
    private val eventRepository: FixedIncomeEventRepository
) {
    operator fun invoke(accountId: String): Flow<FixedIncomeSummary> {
        return combine(
            positionRepository.getByAccount(accountId),
            eventRepository.getByAccount(accountId)
        ) { positions, events ->
            val eventsByPosition = events.groupBy { it.positionId }
            FixedIncomeCalculator.calculateSummary(positions, eventsByPosition)
        }
    }
}