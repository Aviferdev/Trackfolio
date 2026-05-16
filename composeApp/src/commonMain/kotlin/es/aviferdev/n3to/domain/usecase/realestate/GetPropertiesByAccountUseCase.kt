package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository
import kotlinx.coroutines.flow.Flow

class GetPropertiesByAccountUseCase(
    private val repository: RealEstatePropertyRepository
) {
    operator fun invoke(accountId: String): Flow<List<RealEstateProperty>> =
        repository.getPropertiesByAccount(accountId)
}
