package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository
import kotlinx.coroutines.flow.Flow

class GetPropertyByIdUseCase(private val repository: RealEstatePropertyRepository) {
    operator fun invoke(propertyId: String): Flow<RealEstateProperty?> =
        repository.getPropertyById(propertyId)
}
