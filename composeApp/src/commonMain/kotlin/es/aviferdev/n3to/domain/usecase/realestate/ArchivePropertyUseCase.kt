package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository

class ArchivePropertyUseCase(
    private val repository: RealEstatePropertyRepository
) {
    suspend operator fun invoke(propertyId: String): Result<Unit> =
        repository.archiveProperty(propertyId)
}
