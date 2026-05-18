package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNearMaturityPositionsUseCase(
    private val positionRepository: FixedIncomeRepository
) {
    operator fun invoke(accountId: String, daysAhead: Int = 30): Flow<List<FixedIncomePosition>> {
        val now = nowMillis()
        val threshold = now + (daysAhead.toLong() * 24 * 60 * 60 * 1000)
        return positionRepository.getNearMaturity(accountId, threshold)
    }
}