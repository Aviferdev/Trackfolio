package es.aviferdev.trackfolio.data.datasource.issuer

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.IssuerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class IssuerLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : IssuerLocalDataSource {

    private val employers  get() = database.employerQueries
    private val banks      get() = database.bankQueries
    private val bonds      get() = database.bondIssuerQueries
    private val dividends  get() = database.dividendSourceQueries
    private val promotions get() = database.promotionPlatformQueries

    override fun getByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>> =
        when (type) {
            IssuerType.EMPLOYER           -> employers.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
            IssuerType.BANK               -> banks.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
            IssuerType.BOND_ISSUER        -> bonds.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
            IssuerType.DIVIDEND_SOURCE    -> dividends.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
            IssuerType.PROMOTION_PLATFORM -> promotions.selectByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
        }

    override fun getAllByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>> =
        when (type) {
            IssuerType.EMPLOYER           -> employers.selectAllByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
            IssuerType.BANK               -> banks.selectAllByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
            IssuerType.BOND_ISSUER        -> bonds.selectAllByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
            IssuerType.DIVIDEND_SOURCE    -> dividends.selectAllByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
            IssuerType.PROMOTION_PLATFORM -> promotions.selectAllByAccount(accountId).asFlow().mapToList(Dispatchers.IO).map { it.map { e -> e.toDomain() } }
        }

    override fun getById(id: String, type: IssuerType): Flow<Issuer?> =
        when (type) {
            IssuerType.EMPLOYER           -> employers.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() }
            IssuerType.BANK               -> banks.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() }
            IssuerType.BOND_ISSUER        -> bonds.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() }
            IssuerType.DIVIDEND_SOURCE    -> dividends.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() }
            IssuerType.PROMOTION_PLATFORM -> promotions.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() }
        }

    override suspend fun insert(issuer: Issuer): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            when (issuer.type) {
                IssuerType.EMPLOYER           -> employers.insert(issuer.id, issuer.accountId, issuer.name, issuer.icon, issuer.createdAt)
                IssuerType.BANK               -> banks.insert(issuer.id, issuer.accountId, issuer.name, issuer.icon, issuer.createdAt)
                IssuerType.BOND_ISSUER        -> bonds.insert(issuer.id, issuer.accountId, issuer.name, issuer.icon, issuer.createdAt)
                IssuerType.DIVIDEND_SOURCE    -> dividends.insert(issuer.id, issuer.accountId, issuer.name, issuer.icon, issuer.createdAt)
                IssuerType.PROMOTION_PLATFORM -> promotions.insert(issuer.id, issuer.accountId, issuer.name, issuer.icon, issuer.createdAt)
            }
        }
    }

    override suspend fun updateName(id: String, name: String, icon: String, type: IssuerType): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            when (type) {
                IssuerType.EMPLOYER           -> employers.updateName(name = name, icon = icon, id = id)
                IssuerType.BANK               -> banks.updateName(name = name, icon = icon, id = id)
                IssuerType.BOND_ISSUER        -> bonds.updateName(name = name, icon = icon, id = id)
                IssuerType.DIVIDEND_SOURCE    -> dividends.updateName(name = name, icon = icon, id = id)
                IssuerType.PROMOTION_PLATFORM -> promotions.updateName(name = name, icon = icon, id = id)
            }
        }
    }

    override suspend fun archive(id: String, type: IssuerType): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            when (type) {
                IssuerType.EMPLOYER           -> employers.archive(id)
                IssuerType.BANK               -> banks.archive(id)
                IssuerType.BOND_ISSUER        -> bonds.archive(id)
                IssuerType.DIVIDEND_SOURCE    -> dividends.archive(id)
                IssuerType.PROMOTION_PLATFORM -> promotions.archive(id)
            }
        }
    }
}
