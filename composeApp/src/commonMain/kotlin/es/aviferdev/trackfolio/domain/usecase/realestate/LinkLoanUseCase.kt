package es.aviferdev.trackfolio.domain.usecase.realestate

import es.aviferdev.trackfolio.domain.repository.RealEstatePropertyRepository

class LinkLoanUseCase(
    private val repository: RealEstatePropertyRepository
) {
    suspend operator fun invoke(propertyId: String, loanId: String): Result<Unit> =
        repository.linkLoan(propertyId, loanId)
}
