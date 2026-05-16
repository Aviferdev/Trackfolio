package es.aviferdev.trackfolio.domain.usecase.realestate

import es.aviferdev.trackfolio.domain.model.RealEstateProperty
import es.aviferdev.trackfolio.domain.repository.RealEstatePropertyRepository
import kotlinx.coroutines.flow.Flow

class GetPropertiesByAccountUseCase(
    private val repository: RealEstatePropertyRepository
) {
    operator fun invoke(accountId: String): Flow<List<RealEstateProperty>> =
        repository.getPropertiesByAccount(accountId)
}
