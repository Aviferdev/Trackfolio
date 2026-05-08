package es.aviferdev.trackfolio.domain.usecase.fixedincome

import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class GetNearMaturityPositionsUseCase(
    private val positionRepository: FixedIncomeRepository
) {
    operator fun invoke(accountId: String, daysAhead: Int = 30): Flow<List<FixedIncomePosition>> {
        val now = Clock.System.now().toEpochMilliseconds()
        val threshold = now + (daysAhead.toLong() * 24 * 60 * 60 * 1000)
        return positionRepository.getNearMaturity(accountId, threshold)
    }
}