package es.aviferdev.n3to.domain.usecase.portfolio

import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow

class GetPortfoliosByAccountUseCase(
    private val repository: PortfolioRepository
) {
    operator fun invoke(accountId: String): Flow<List<Portfolio>> =
        repository.getByAccount(accountId)
}
