package es.aviferdev.n3to.data.datasource.realestate

import es.aviferdev.n3to.domain.model.RentalPeriod
import kotlinx.coroutines.flow.Flow

interface RentalPeriodLocalDataSource {
    fun getByProperty(propertyId: String): Flow<List<RentalPeriod>>
    fun getActiveByProperty(propertyId: String): Flow<RentalPeriod?>
    suspend fun insert(period: RentalPeriod): Result<Unit>
    suspend fun closePeriod(periodId: String, endDate: Long): Result<Unit>
}
