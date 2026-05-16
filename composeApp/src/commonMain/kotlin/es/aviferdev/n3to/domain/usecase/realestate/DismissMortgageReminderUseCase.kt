package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository

class DismissMortgageReminderUseCase(
    private val repository: RealEstatePropertyRepository
) {
    suspend operator fun invoke(propertyId: String): Result<Unit> =
        repository.dismissMortgageReminder(propertyId)
}
