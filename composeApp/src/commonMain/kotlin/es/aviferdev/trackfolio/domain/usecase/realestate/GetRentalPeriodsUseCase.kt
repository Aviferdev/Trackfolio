package es.aviferdev.trackfolio.domain.usecase.realestate

import es.aviferdev.trackfolio.domain.model.RentalPeriod
import es.aviferdev.trackfolio.domain.repository.RentalPeriodRepository
import kotlinx.coroutines.flow.Flow

class GetRentalPeriodsUseCase(
    private val repository: RentalPeriodRepository
) {
    operator fun invoke(propertyId: String): Flow<List<RentalPeriod>> =
        repository.getRentalPeriodsByProperty(propertyId)
}
