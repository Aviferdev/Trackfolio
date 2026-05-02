package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.data.datasource.AccountLocalDataSource
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val dataSource: AccountLocalDataSource
) : AccountRepository {

    override fun getAll(): Flow<List<Account>> =
        dataSource.getAll().map { it.map { entity -> entity.toDomain() } }

    override fun getById(id: String): Flow<Account?> =
        dataSource.getById(id).map { it?.toDomain() }

    override fun getTotalBalance(): Flow<Double> =
        dataSource.getTotalBalance()

    override suspend fun save(account: Account): Result<Unit> =
        dataSource.insert(account.toEntity())

    override suspend fun delete(id: String): Result<Unit> =
        dataSource.delete(id)
}
