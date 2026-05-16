package es.aviferdev.trackfolio.domain.usecase.realestate

import es.aviferdev.trackfolio.domain.repository.RealEstatePropertyRepository

class DismissMortgageReminderUseCase(
    private val repository: RealEstatePropertyRepository
) {
    suspend operator fun invoke(propertyId: String): Result<Unit> =
        repository.dismissMortgageReminder(propertyId)
}
