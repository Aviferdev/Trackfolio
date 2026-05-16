package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
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