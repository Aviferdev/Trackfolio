package es.aviferdev.n3to.domain.usecase.assetplatform

import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.repository.AssetPlatformRepository
import kotlinx.coroutines.flow.Flow

class GetPlatformsByAssetUseCase(private val repository: AssetPlatformRepository) {
    operator fun invoke(assetId: String): Flow<List<Platform>> =
        repository.getPlatformsByAsset(assetId)
}

class GetPlatformsByAssetsUseCase(private val repository: AssetPlatformRepository) {
    operator fun invoke(assetIds: List<String>): Flow<Map<String, List<Platform>>> =
        repository.getPlatformsByAssets(assetIds)
}

class LinkPlatformToAssetUseCase(private val repository: AssetPlatformRepository) {
    suspend operator fun invoke(assetId: String, platformId: String): Result<Unit> =
        repository.link(assetId, platformId)
}

class UnlinkPlatformFromAssetUseCase(private val repository: AssetPlatformRepository) {
    suspend operator fun invoke(assetId: String, platformId: String): Result<Unit> =
        repository.unlink(assetId, platformId)
}

class UnlinkAllPlatformsFromAssetUseCase(private val repository: AssetPlatformRepository) {
    suspend operator fun invoke(assetId: String): Result<Unit> =
        repository.unlinkAllByAsset(assetId)
}
