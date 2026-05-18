package es.aviferdev.n3to.domain.usecase.platform

import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.repository.PlatformCategoryRepository
import es.aviferdev.n3to.domain.repository.PlatformRepository
import kotlinx.coroutines.flow.Flow

class GetPlatformsUseCase(private val repository: PlatformRepository) {
    operator fun invoke(): Flow<List<Platform>> = repository.getAll()
}

class GetAllPlatformsIncludingArchivedUseCase(private val repository: PlatformRepository) {
    operator fun invoke(): Flow<List<Platform>> = repository.getAllIncludingArchived()
}

class SavePlatformUseCase(private val repository: PlatformRepository) {
    suspend operator fun invoke(platform: Platform): Result<Unit> = repository.save(platform)
}

class RenamePlatformUseCase(private val repository: PlatformRepository) {
    suspend operator fun invoke(id: String, newName: String, newIcon: String, notes: String?): Result<Unit> =
        repository.rename(id, newName, newIcon, notes)
}

class ArchivePlatformUseCase(private val repository: PlatformRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.archive(id)
}

class GetPlatformsByCategoryUseCase(private val repository: PlatformCategoryRepository) {
    operator fun invoke(categoryId: String): Flow<List<Platform>> =
        repository.getByCategory(categoryId)
}

class LinkPlatformToCategoryUseCase(private val repository: PlatformCategoryRepository) {
    suspend operator fun invoke(platformId: String, categoryId: String): Result<Unit> =
        repository.link(platformId, categoryId)
}

class UnlinkPlatformFromCategoryUseCase(private val repository: PlatformCategoryRepository) {
    suspend operator fun invoke(platformId: String, categoryId: String): Result<Unit> =
        repository.unlink(platformId, categoryId)
}

class CreateAndLinkPlatformUseCase(
    private val platformRepository: PlatformRepository,
    private val platformCategoryRepository: PlatformCategoryRepository
) {
    suspend operator fun invoke(
        name: String,
        icon: String,
        notes: String?,
        categoryId: String,
        sortOrder: Int,
        createdAt: Long
    ): Result<Unit> {
        val platform = Platform(
            id        = "platform_$createdAt",
            name      = name,
            icon      = icon.ifBlank { "🏦" },
            sortOrder = sortOrder,
            createdAt = createdAt,
            notes     = notes
        )
        val result = platformRepository.save(platform)
        if (result.isFailure) return result
        return platformCategoryRepository.link(platform.id, categoryId)
    }
}
