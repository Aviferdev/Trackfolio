package es.aviferdev.n3to.data.repository.fixedincome

import es.aviferdev.n3to.data.datasource.fixedincome.FixedIncomeEventLocalDataSource
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import kotlinx.coroutines.flow.Flow

class FixedIncomeEventRepositoryImpl(
    private val localDataSource: FixedIncomeEventLocalDataSource
) : FixedIncomeEventRepository {

    override fun getByPosition(positionId: String): Flow<List<FixedIncomeEvent>> =
        localDataSource.getByPosition(positionId)

    override fun getByAccount(accountId: String): Flow<List<FixedIncomeEvent>> =
        localDataSource.getByAccount(accountId)

    override fun totalCollectedByPosition(positionId: String): Flow<Double> =
        localDataSource.totalCollectedByPosition(positionId)

    override suspend fun insert(event: FixedIncomeEvent): Result<Unit> =
        localDataSource.insert(event)

    override suspend fun update(event: FixedIncomeEvent): Result<Unit> =
        localDataSource.update(event)

    override suspend fun delete(id: String): Result<Unit> =
        localDataSource.delete(id)
}