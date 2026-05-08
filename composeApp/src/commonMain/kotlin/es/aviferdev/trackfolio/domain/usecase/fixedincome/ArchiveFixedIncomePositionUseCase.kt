package es.aviferdev.trackfolio.domain.usecase.fixedincome

import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository

class ArchiveFixedIncomePositionUseCase(
    private val repository: FixedIncomeRepository
) {
    suspend operator fun invoke(positionId: String): Result<Unit> =
        repository.archive(positionId)
}