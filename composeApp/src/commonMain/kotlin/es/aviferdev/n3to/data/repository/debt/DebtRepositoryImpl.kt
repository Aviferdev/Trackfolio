package es.aviferdev.n3to.data.repository.debt

import es.aviferdev.n3to.data.datasource.debt.DebtLocalDataSource
import es.aviferdev.n3to.domain.model.Debt
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow

class DebtRepositoryImpl(
    private val dataSource: DebtLocalDataSource
) : DebtRepository {

    override fun getActiveByAccount(accountId: String): Flow<List<Debt>> =
        dataSource.getActiveByAccount(accountId)

    override fun getActive(): Flow<List<Debt>> =
        dataSource.getActive()

    override fun getAll(): Flow<List<Debt>> =
        dataSource.getAll()

    override fun getByAccount(accountId: String): Flow<List<Debt>> =
        dataSource.getByAccount(accountId)

    override fun getTotalByDirection(direction: DebtDirection): Flow<Double> =
        dataSource.getTotalByDirection(direction.name)

    override fun getTotalByDirectionAndAccount(
        accountId: String,
        direction: DebtDirection
    ): Flow<Double> =
        dataSource.getTotalByDirectionAndAccount(accountId, direction.name)

    override suspend fun save(debt: Debt): Result<Unit> =
        dataSource.insert(debt)

    override suspend fun update(debt: Debt): Result<Unit> =
        dataSource.update(debt)

    override suspend fun markAsPaid(id: String): Result<Unit> =
        dataSource.markAsPaid(id)

    override suspend fun archive(id: String): Result<Unit> =
        dataSource.archive(id)

    override suspend fun unarchive(id: String): Result<Unit> =
        dataSource.unarchive(id)

    override suspend fun delete(id: String): Result<Unit> =
        dataSource.delete(id)
}
