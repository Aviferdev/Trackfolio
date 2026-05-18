package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetComposition
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import es.aviferdev.n3to.domain.repository.AssetMetadataRepository
import es.aviferdev.n3to.domain.repository.AssetPlatformRepository

class SaveAssetWithMetadataUseCase(
    private val saveAsset: SaveAssetUseCase,
    private val assetMetadataRepository: AssetMetadataRepository,
    private val assetPlatformRepository: AssetPlatformRepository
) {
    suspend operator fun invoke(
        asset: Asset,
        fixedIncomePercent: Int = 0,
        sectorIds: Set<String> = emptySet(),
        regionPercents: Map<String, Int> = emptyMap(),
        platformIds: Set<String> = emptySet()
    ): Result<Unit> {
        val result = saveAsset(asset)
        if (result.isFailure) return result

        val now = asset.createdAt
        if (fixedIncomePercent > 0) {
            assetMetadataRepository.saveComposition(
                AssetComposition(assetId = asset.id, fixedIncomePercent = fixedIncomePercent, createdAt = now)
            )
        }
        sectorIds.forEach { sectorId ->
            assetMetadataRepository.saveSectorRelation(AssetSectorRelation(assetId = asset.id, sectorId = sectorId))
        }
        regionPercents.forEach { (regionId, percent) ->
            if (percent > 0) {
                assetMetadataRepository.saveRegionDistribution(
                    AssetRegionDistribution(assetId = asset.id, regionId = regionId, percent = percent)
                )
            }
        }
        platformIds.forEach { platId ->
            assetPlatformRepository.link(asset.id, platId)
        }
        return Result.success(Unit)
    }
}
