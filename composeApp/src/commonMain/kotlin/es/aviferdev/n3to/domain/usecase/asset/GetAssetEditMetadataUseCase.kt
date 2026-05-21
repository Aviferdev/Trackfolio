package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.repository.AssetMetadataRepository
import es.aviferdev.n3to.domain.repository.AssetPlatformRepository
import kotlinx.coroutines.flow.first

data class AssetEditMetadata(
    val platformIds: Set<String>,
    val sectorIds: Set<String>,
    val regionPercents: Map<String, Int>,
    val fixedIncomePercent: Int
)

class GetAssetEditMetadataUseCase(
    private val assetPlatformRepository: AssetPlatformRepository,
    private val assetMetadataRepository: AssetMetadataRepository
) {
    suspend operator fun invoke(assetId: String): AssetEditMetadata {
        val platforms = assetPlatformRepository.getPlatformsByAsset(assetId).first()
        val sectors = assetMetadataRepository.getSectorsByAssetId(assetId).first()
        val regions = assetMetadataRepository.getRegionDistributionsByAssetId(assetId).first()
        val composition = assetMetadataRepository.getCompositionByAssetId(assetId).first()
        return AssetEditMetadata(
            platformIds = platforms.map { it.id }.toSet(),
            sectorIds = sectors.map { it.id }.toSet(),
            regionPercents = regions.associate { it.regionId to it.percent },
            fixedIncomePercent = composition?.fixedIncomePercent ?: 0
        )
    }
}
