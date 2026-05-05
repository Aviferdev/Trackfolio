package es.aviferdev.trackfolio.domain.usecase.assetcategory

import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.repository.AssetCategoryRepository
import kotlinx.coroutines.flow.Flow

class GetAssetCategoriesUseCase(
    private val repository: AssetCategoryRepository
) {
    operator fun invoke(): Flow<List<AssetCategory>> = repository.getAll()
}

class GetAllAssetCategoriesIncludingArchivedUseCase(
    private val repository: AssetCategoryRepository
) {
    operator fun invoke(): Flow<List<AssetCategory>> = repository.getAllIncludingArchived()
}

class SaveAssetCategoryUseCase(
    private val repository: AssetCategoryRepository
) {
    suspend operator fun invoke(category: AssetCategory): Result<Unit> = repository.save(category)
}

class RenameAssetCategoryUseCase(
    private val repository: AssetCategoryRepository
) {
    suspend operator fun invoke(id: String, newName: String, newIcon: String): Result<Unit> =
        repository.rename(id, newName, newIcon)
}

class ArchiveAssetCategoryUseCase(
    private val repository: AssetCategoryRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.archive(id)
}
