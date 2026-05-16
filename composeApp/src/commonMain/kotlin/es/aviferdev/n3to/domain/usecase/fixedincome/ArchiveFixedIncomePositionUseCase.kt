package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.domain.repository.FixedIncomeRepository

class ArchiveFixedIncomePositionUseCase(
    private val repository: FixedIncomeRepository
) {
    suspend operator fun invoke(positionId: String): Result<Unit> =
        repository.archive(positionId)
}