package es.aviferdev.n3to.domain.usecase.portfolio

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.repository.PortfolioRepository

class SavePortfolioUseCase(private val repository: PortfolioRepository) {
    suspend operator fun invoke(
        accountId: String,
        name: String,
        description: String? = null,
        color: String? = null
    ): Result<Unit> {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val id = "port_" + (1..20).map { chars.random() }.joinToString("")
        val portfolio = Portfolio(
            id          = id,
            accountId   = accountId,
            name        = name,
            description = description,
            color       = color,
            sortOrder   = 0,
            createdAt   = nowMillis()
        )
        return repository.save(portfolio)
    }
}

class UpdatePortfolioUseCase(private val repository: PortfolioRepository) {
    suspend operator fun invoke(portfolio: Portfolio): Result<Unit> =
        repository.update(portfolio)
}

class DeletePortfolioUseCase(private val repository: PortfolioRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.delete(id)
}
