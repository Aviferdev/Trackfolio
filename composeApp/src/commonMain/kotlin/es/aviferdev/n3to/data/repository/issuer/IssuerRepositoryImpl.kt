package es.aviferdev.n3to.data.repository.issuer

import es.aviferdev.n3to.data.datasource.issuer.IssuerLocalDataSource
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType
import es.aviferdev.n3to.domain.repository.IssuerRepository
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

    override suspend fun updateName(
        id: String,
        name: String,
        icon: String,
        type: IssuerType
    ): Result<Unit> =
        dataSource.updateName(id, name, icon, type)

    override suspend fun archive(id: String, type: IssuerType): Result<Unit> =
        dataSource.archive(id, type)
}