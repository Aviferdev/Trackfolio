package es.aviferdev.n3to.data.repository.realestate

import es.aviferdev.n3to.data.datasource.realestate.RentalPeriodLocalDataSource
import es.aviferdev.n3to.domain.model.RentalPeriod
import es.aviferdev.n3to.domain.repository.RentalPeriodRepository
import kotlinx.coroutines.flow.Flow

class RentalPeriodRepositoryImpl(
    private val localDataSource: RentalPeriodLocalDataSource
) : RentalPeriodRepository {

    override fun getRentalPeriodsByProperty(propertyId: String): Flow<List<RentalPeriod>> =
        localDataSource.getByProperty(propertyId)

    override fun getActivePeriodByProperty(propertyId: String): Flow<RentalPeriod?> =
        localDataSource.getActiveByProperty(propertyId)

    override suspend fun openRentalPeriod(period: RentalPeriod): Result<Unit> =
        localDataSource.insert(period)

    override suspend fun closeRentalPeriod(periodId: String, endDate: Long): Result<Unit> =
        localDataSource.closePeriod(periodId, endDate)
}
