package es.aviferdev.n3to.data.datasource.realestate

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.RentalPeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RentalPeriodLocalDataSourceImpl(
    private val database: N3toDatabase
) : RentalPeriodLocalDataSource {

    private val queries = database.rentalPeriodQueries

    override fun getByProperty(propertyId: String): Flow<List<RentalPeriod>> =
        queries.selectByProperty(propertyId)
            .asFlow().mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getActiveByProperty(propertyId: String): Flow<RentalPeriod?> =
        queries.selectActiveByProperty(propertyId)
            .asFlow().mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun insert(period: RentalPeriod): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val e = period.toEntity()
            queries.insert(e.id, e.propertyId, e.startDate, e.endDate, e.monthlyRent, e.notes)
        }
    }

    override suspend fun closePeriod(periodId: String, endDate: Long): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                queries.closePeriod(
                    id = periodId,
                    endDate = endDate
                )
            }
        }
}
