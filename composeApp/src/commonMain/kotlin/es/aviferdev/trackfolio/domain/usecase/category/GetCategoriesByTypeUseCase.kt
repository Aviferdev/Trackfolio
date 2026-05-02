package es.aviferdev.trackfolio.domain.usecase.category

import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesByTypeUseCase(private val repository: CategoryRepository) {
    operator fun invoke(type: TransactionType): Flow<List<Category>> = repository.getByType(type)
}
