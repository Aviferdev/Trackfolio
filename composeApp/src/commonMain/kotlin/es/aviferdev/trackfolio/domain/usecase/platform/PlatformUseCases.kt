package es.aviferdev.trackfolio.domain.usecase.platform

import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.repository.PlatformRepository
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
