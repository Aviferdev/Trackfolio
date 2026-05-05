package es.aviferdev.trackfolio.domain.usecase.category

import es.aviferdev.trackfolio.domain.model.Category
import es.aviferdev.trackfolio.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Devuelve todas las categorías incluyendo archivadas.
 * Úsalo en pantallas que renderizan transacciones existentes para
 * poder mostrar el nombre de la categoría aunque haya sido archivada.
 */
class GetAllCategoriesIncludingArchivedUseCase(
    private val repository: CategoryRepository
) {
    operator fun invoke(): Flow<List<Category>> = repository.getAllIncludingArchived()
}
