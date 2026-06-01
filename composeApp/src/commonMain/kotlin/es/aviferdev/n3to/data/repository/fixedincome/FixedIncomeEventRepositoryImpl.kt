package es.aviferdev.n3to.data.repository.fixedincome

import es.aviferdev.n3to.data.datasource.fixedincome.FixedIncomeLocalDataSource
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import kotlinx.coroutines.flow.Flow

class FixedIncomeEventRepositoryImpl(
    private val fixedIncomeDataSource: FixedIncomeLocalDataSource
) : FixedIncomeEventRepository {

    override fun getByPosition(positionId: String): Flow<List<FixedIncomeEvent>> =
        fixedIncomeDataSource.getEventsByPosition(positionId)

    override fun getByAccount(accountId: String): Flow<List<FixedIncomeEvent>> =
        fixedIncomeDataSource.getEventsByAccount(accountId)

    override fun totalCollectedByPosition(positionId: String): Flow<Double> =
        fixedIncomeDataSource.totalCollectedByPosition(positionId)

    override suspend fun insert(event: FixedIncomeEvent): Result<Unit> =
        fixedIncomeDataSource.insertEvent(event)

    override suspend fun update(event: FixedIncomeEvent): Result<Unit> =
        fixedIncomeDataSource.updateEvent(event)

    override suspend fun delete(id: String): Result<Unit> =
        fixedIncomeDataSource.deleteEvent(id)
}
