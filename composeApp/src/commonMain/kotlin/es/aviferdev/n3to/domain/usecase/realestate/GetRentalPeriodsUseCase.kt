package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.model.RentalPeriod
import es.aviferdev.n3to.domain.repository.RentalPeriodRepository
import kotlinx.coroutines.flow.Flow

class GetRentalPeriodsUseCase(
    private val repository: RentalPeriodRepository
) {
    operator fun invoke(propertyId: String): Flow<List<RentalPeriod>> =
        repository.getRentalPeriodsByProperty(propertyId)
}
