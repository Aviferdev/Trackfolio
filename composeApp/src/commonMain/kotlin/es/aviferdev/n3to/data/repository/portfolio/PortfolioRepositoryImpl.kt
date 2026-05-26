package es.aviferdev.n3to.data.repository.portfolio

import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.data.datasource.portfolio.PortfolioLocalDataSource
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PortfolioRepositoryImpl(
    private val dataSource: PortfolioLocalDataSource
) : PortfolioRepository {

    override fun getByAccount(accountId: String): Flow<List<Portfolio>> =
        dataSource.getByAccount(accountId).map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<Portfolio?> =
        dataSource.getById(id).map { it?.toDomain() }

    override suspend fun save(portfolio: Portfolio): Result<Unit> =
        runCatching { dataSource.insert(portfolio.toEntity()) }

    override suspend fun update(portfolio: Portfolio): Result<Unit> =
        runCatching { dataSource.update(portfolio.toEntity()) }

    override suspend fun archive(id: String): Result<Unit> =
        runCatching { dataSource.archive(id) }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching { dataSource.delete(id) }
}
