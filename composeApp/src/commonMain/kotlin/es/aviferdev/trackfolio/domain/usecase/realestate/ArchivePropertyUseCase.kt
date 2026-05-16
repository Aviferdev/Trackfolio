package es.aviferdev.trackfolio.domain.usecase.realestate

import es.aviferdev.trackfolio.domain.repository.RealEstatePropertyRepository

class ArchivePropertyUseCase(
    private val repository: RealEstatePropertyRepository
) {
    suspend operator fun invoke(propertyId: String): Result<Unit> =
        repository.archiveProperty(propertyId)
}
