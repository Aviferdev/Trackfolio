package es.aviferdev.n3to.domain.usecase.category

import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesByTypeUseCase(private val repository: CategoryRepository) {
    operator fun invoke(type: TransactionType): Flow<List<Category>> = repository.getByType(type)
}
