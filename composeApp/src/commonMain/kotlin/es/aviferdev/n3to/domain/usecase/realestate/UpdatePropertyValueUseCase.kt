package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository

class UpdatePropertyValueUseCase(
    private val repository: RealEstatePropertyRepository
) {
    suspend operator fun invoke(propertyId: String, newValue: Double): Result<Unit> {
        require(newValue > 0) { "El valor estimado debe ser mayor que 0" }
        return repository.updateEstimatedValue(propertyId, newValue)
    }
}
