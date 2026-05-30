package es.aviferdev.n3to.data.datasource.issuer

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class IssuerLocalDataSourceImpl(
    private val database: N3toDatabase
) : IssuerLocalDataSource {

    private val queries get() = database.issuerQueries

    override fun getByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>> =
        queries.selectByAccount(accountId, type.name)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { e -> e.toDomain() } }

    override fun getAllByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>> =
        queries.selectAllByAccount(accountId, type.name)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { e -> e.toDomain() } }

    override fun getById(id: String, type: IssuerType): Flow<Issuer?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun insert(issuer: Issuer): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.insert(
                issuer.id,
                issuer.accountId,
                issuer.name,
                issuer.icon,
                issuer.type.name,
                issuer.createdAt
            )
        }
    }

    override suspend fun updateName(
        id: String,
        name: String,
        icon: String,
        type: IssuerType
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.updateName(name = name, icon = icon, id = id)
        }
    }

    override suspend fun archive(id: String, type: IssuerType): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            queries.archive(id)
        }
    }
}
