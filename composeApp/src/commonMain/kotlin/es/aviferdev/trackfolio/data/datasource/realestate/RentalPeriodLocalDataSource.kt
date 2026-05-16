package es.aviferdev.trackfolio.data.datasource.realestate

import es.aviferdev.trackfolio.domain.model.RentalPeriod
import kotlinx.coroutines.flow.Flow

interface RentalPeriodLocalDataSource {
    fun getByProperty(propertyId: String): Flow<List<RentalPeriod>>
    fun getActiveByProperty(propertyId: String): Flow<RentalPeriod?>
    suspend fun insert(period: RentalPeriod): Result<Unit>
    suspend fun closePeriod(periodId: String, endDate: Long): Result<Unit>
}
