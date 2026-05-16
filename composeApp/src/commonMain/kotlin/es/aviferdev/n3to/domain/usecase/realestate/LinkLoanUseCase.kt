package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository

class LinkLoanUseCase(
    private val repository: RealEstatePropertyRepository
) {
    suspend operator fun invoke(propertyId: String, loanId: String): Result<Unit> =
        repository.linkLoan(propertyId, loanId)
}
