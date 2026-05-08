package es.aviferdev.trackfolio.data.repository.fixedincome

import es.aviferdev.trackfolio.data.datasource.fixedincome.FixedIncomeLocalDataSource
import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository
import kotlinx.coroutines.flow.Flow

class FixedIncomeRepositoryImpl(
    private val localDataSource: FixedIncomeLocalDataSource
) : FixedIncomeRepository {

    override fun getByAccount(accountId: String): Flow<List<FixedIncomePosition>> =
        localDataSource.getByAccount(accountId)

    override fun getOpenByAccount(accountId: String): Flow<List<FixedIncomePosition>> =
        localDataSource.getOpenByAccount(accountId)

    override fun getById(id: String): Flow<FixedIncomePosition?> =
        localDataSource.getById(id)

    override fun getNearMaturity(accountId: String, thresholdDate: Long): Flow<List<FixedIncomePosition>> =
        localDataSource.getNearMaturity(accountId, thresholdDate)

    override suspend fun insert(position: FixedIncomePosition): Result<Unit> =
        localDataSource.insert(position)

    override suspend fun update(position: FixedIncomePosition): Result<Unit> =
        localDataSource.update(position)

    override suspend fun close(id: String, closedAt: Long, closeType: String): Result<Unit> =
        localDataSource.close(id, closedAt, closeType)

    override suspend fun archive(id: String): Result<Unit> =
        localDataSource.archive(id)
}