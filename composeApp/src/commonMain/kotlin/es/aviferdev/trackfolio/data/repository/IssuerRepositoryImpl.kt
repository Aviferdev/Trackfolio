package es.aviferdev.trackfolio.data.repository

import es.aviferdev.trackfolio.data.datasource.IssuerLocalDataSource
import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.IssuerType
import es.aviferdev.trackfolio.domain.repository.IssuerRepository
import kotlinx.coroutines.flow.Flow

class IssuerRepositoryImpl(
    private val dataSource: IssuerLocalDataSource
) : IssuerRepository {

    override fun getByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>> =
        dataSource.getByAccount(accountId, type)

    override fun getAllByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>> =
        dataSource.getAllByAccount(accountId, type)

    override fun getById(id: String, type: IssuerType): Flow<Issuer?> =
        dataSource.getById(id, type)

    override suspend fun save(issuer: Issuer): Result<Unit> =
        dataSource.insert(issuer)

    override suspend fun updateName(id: String, name: String, icon: String, type: IssuerType): Result<Unit> =
        dataSource.updateName(id, name, icon, type)

    override suspend fun archive(id: String, type: IssuerType): Result<Unit> =
        dataSource.archive(id, type)
}
