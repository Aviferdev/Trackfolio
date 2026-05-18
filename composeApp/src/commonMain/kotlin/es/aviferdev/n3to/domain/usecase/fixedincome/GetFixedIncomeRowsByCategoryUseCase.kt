package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetFixedIncomeRowsByCategoryUseCase(
    private val fixedIncomeRepository: FixedIncomeRepository,
    private val fixedIncomeEventRepository: FixedIncomeEventRepository
) {
    operator fun invoke(accountId: String, categoryId: String): Flow<List<FixedIncomeRow>> =
        combine(
            fixedIncomeRepository.getByAccountAndCategory(accountId, categoryId),
            fixedIncomeEventRepository.getByAccount(accountId)
        ) { positions, events ->
            val eventsByPosition = events.groupBy { it.positionId }
            positions.map { pos ->
                FixedIncomeCalculator.calculatePosition(pos, eventsByPosition[pos.id] ?: emptyList())
            }
        }
}
