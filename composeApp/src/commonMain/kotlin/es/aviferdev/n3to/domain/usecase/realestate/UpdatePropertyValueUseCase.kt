package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.model.ValidationError
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository

class UpdatePropertyValueUseCase(
    private val repository: RealEstatePropertyRepository
) {
    suspend operator fun invoke(propertyId: String, newValue: Double): Result<Unit> {
        if (newValue <= 0) return Result.failure(ValidationError.EstimatedValueInvalid)
        return repository.updateEstimatedValue(propertyId, newValue)
    }
}
