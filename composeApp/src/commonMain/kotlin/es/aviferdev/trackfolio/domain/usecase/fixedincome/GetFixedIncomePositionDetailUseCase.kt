package es.aviferdev.trackfolio.domain.usecase.fixedincome

import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.model.FixedIncomeRow
import es.aviferdev.trackfolio.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.trackfolio.domain.repository.FixedIncomeEventRepository
import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFixedIncomePositionDetailUseCase(
    private val positionRepository: FixedIncomeRepository,
    private val eventRepository: FixedIncomeEventRepository
) {
    fun getPosition(positionId: String): Flow<FixedIncomeRow?> {
        return combine(
            positionRepository.getById(positionId),
            eventRepository.getByPosition(positionId)
        ) { position, events ->
            position?.let { FixedIncomeCalculator.calculatePosition(it, events) }
        }
    }

    fun getEvents(positionId: String): Flow<List<FixedIncomeEvent>> =
        eventRepository.getByPosition(positionId)
}