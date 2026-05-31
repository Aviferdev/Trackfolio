package es.aviferdev.n3to.data.repository.portfolio

import es.aviferdev.n3to.data.datasource.portfolio.PortfolioLocalDataSource
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow

class PortfolioRepositoryImpl(
    private val dataSource: PortfolioLocalDataSource
) : PortfolioRepository {

    override fun getByAccount(accountId: String): Flow<List<Portfolio>> =
        dataSource.getByAccount(accountId)

    override fun getArchivedByAccount(accountId: String): Flow<List<Portfolio>> =
        dataSource.getArchivedByAccount(accountId)

    override fun getById(id: String): Flow<Portfolio?> =
        dataSource.getById(id)

    override suspend fun save(portfolio: Portfolio): Result<Unit> =
        runCatching { dataSource.insert(portfolio) }

    override suspend fun update(portfolio: Portfolio): Result<Unit> =
        runCatching { dataSource.update(portfolio) }

    override suspend fun archive(id: String): Result<Unit> =
        runCatching { dataSource.archive(id) }

    override suspend fun unarchive(id: String): Result<Unit> =
        runCatching { dataSource.unarchive(id) }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching { dataSource.delete(id) }
}
