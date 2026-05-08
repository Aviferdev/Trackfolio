package es.aviferdev.trackfolio.data.datasource.fixedincome

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import es.aviferdev.trackfolio.data.database.TrackfolioDatabase
import es.aviferdev.trackfolio.data.database.mapper.toDomain
import es.aviferdev.trackfolio.data.database.mapper.toEntity
import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FixedIncomeEventLocalDataSourceImpl(
    private val database: TrackfolioDatabase
) : FixedIncomeEventLocalDataSource {

    private val queries = database.fixedIncomeEventQueries

    override fun getByPosition(positionId: String): Flow<List<FixedIncomeEvent>> =
        queries.selectByPosition(positionId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getByAccount(accountId: String): Flow<List<FixedIncomeEvent>> =
        queries.selectByAccount(accountId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun totalCollectedByPosition(positionId: String): Flow<Double> =
        queries.totalCollectedByPosition(positionId)
            .asFlow()
            .mapToOne(Dispatchers.IO)

    override suspend fun insert(event: FixedIncomeEvent): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = event.toEntity()
                queries.insert(
                    id               = e.id,
                    positionId       = e.positionId,
                    type             = e.type,
                    grossAmount      = e.grossAmount,
                    irpfPercent      = e.irpfPercent,
                    commissionAmount = e.commissionAmount,
                    netAmount        = e.netAmount,
                    date             = e.date,
                    notes            = e.notes,
                    createdAt        = e.createdAt
                )
            }
        }

    override suspend fun update(event: FixedIncomeEvent): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = event.toEntity()
                queries.update(
                    id               = e.id,
                    type             = e.type,
                    grossAmount      = e.grossAmount,
                    irpfPercent      = e.irpfPercent,
                    commissionAmount = e.commissionAmount,
                    netAmount        = e.netAmount,
                    date             = e.date,
                    notes            = e.notes
                )
            }
        }

    override suspend fun delete(id: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { queries.delete(id) }
        }
}