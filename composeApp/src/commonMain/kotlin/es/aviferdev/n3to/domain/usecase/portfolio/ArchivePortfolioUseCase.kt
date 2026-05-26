package es.aviferdev.n3to.domain.usecase.portfolio

import es.aviferdev.n3to.domain.repository.PortfolioRepository

class ArchivePortfolioUseCase(private val repository: PortfolioRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.archive(id)
}
