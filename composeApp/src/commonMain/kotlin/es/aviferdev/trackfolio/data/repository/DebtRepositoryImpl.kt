package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.data.datasource.DebtLocalDataSource
import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.model.DebtDirection
import es.aviferdev.trackfolio.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DebtRepositoryImpl(
    private val dataSource: DebtLocalDataSource
) : DebtRepository {

    override fun getActive(): Flow<List<Debt>> =
        dataSource.getActive().map { it.map { entity -> entity.toDomain() } }

    override fun getAll(): Flow<List<Debt>> =
        dataSource.getAll().map { it.map { entity -> entity.toDomain() } }

    override fun getTotalByDirection(direction: DebtDirection): Flow<Double> =
        dataSource.getTotalByDirection(direction.name)

    override suspend fun save(debt: Debt): Result<Unit> =
        dataSource.insert(debt.toEntity())

    override suspend fun update(debt: Debt): Result<Unit> =
        dataSource.update(debt.toEntity())

    override suspend fun markAsPaid(id: String): Result<Unit> =
        dataSource.markAsPaid(id)

    override suspend fun delete(id: String): Result<Unit> =
        dataSource.delete(id)
}
