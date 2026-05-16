package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.RentalPeriod
import kotlinx.coroutines.flow.Flow

interface RentalPeriodRepository {
    fun getRentalPeriodsByProperty(propertyId: String): Flow<List<RentalPeriod>>
    fun getActivePeriodByProperty(propertyId: String): Flow<RentalPeriod?>
    suspend fun openRentalPeriod(period: RentalPeriod): Result<Unit>
    suspend fun closeRentalPeriod(periodId: String, endDate: Long): Result<Unit>
}
