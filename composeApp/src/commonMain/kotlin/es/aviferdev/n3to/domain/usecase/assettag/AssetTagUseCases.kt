package es.aviferdev.n3to.domain.usecase.assettag

import es.aviferdev.n3to.domain.model.AssetTag
import es.aviferdev.n3to.domain.model.AssetTagAssignment
import es.aviferdev.n3to.domain.repository.AssetTagRepository
import kotlinx.coroutines.flow.Flow

class GetAssetTagsUseCase(private val repository: AssetTagRepository) {
    operator fun invoke(): Flow<List<AssetTag>> = repository.getAllTags()
}

class GetAssetTagsByCategoryUseCase(private val repository: AssetTagRepository) {
    operator fun invoke(categoryId: String): Flow<List<AssetTag>> =
        repository.getTagsByCategory(categoryId)
}

class SaveAssetTagUseCase(private val repository: AssetTagRepository) {
    suspend operator fun invoke(tag: AssetTag): Result<Unit> = repository.saveTag(tag)
}

class RenameAssetTagUseCase(private val repository: AssetTagRepository) {
    suspend operator fun invoke(id: String, newName: String, newColor: String): Result<Unit> =
        repository.renameTag(id, newName, newColor)
}

class ArchiveAssetTagUseCase(private val repository: AssetTagRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.archiveTag(id)
}

class GetAssetTagAssignmentsUseCase(private val repository: AssetTagRepository) {
    operator fun invoke(assetId: String): Flow<List<AssetTagAssignment>> =
        repository.getAssignmentsForAsset(assetId)
}

class UpsertAssetTagAssignmentUseCase(private val repository: AssetTagRepository) {
    suspend operator fun invoke(assetId: String, tagId: String, weight: Double): Result<Unit> =
        repository.upsertAssignment(assetId, tagId, weight)
}

class RemoveAssetTagAssignmentUseCase(private val repository: AssetTagRepository) {
    suspend operator fun invoke(assetId: String, tagId: String): Result<Unit> =
        repository.removeAssignment(assetId, tagId)
}
