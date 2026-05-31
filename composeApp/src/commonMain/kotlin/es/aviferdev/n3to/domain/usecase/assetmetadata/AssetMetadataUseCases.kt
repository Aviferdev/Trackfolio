package es.aviferdev.n3to.domain.usecase.assetmetadata

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import es.aviferdev.n3to.domain.repository.AssetMetadataRepository
import kotlinx.coroutines.flow.Flow

class GetSectorsUseCase(private val repository: AssetMetadataRepository) {
    operator fun invoke(): Flow<List<AssetSector>> = repository.getAllSectors()
}

class GetSectorByIdUseCase(private val repository: AssetMetadataRepository) {
    operator fun invoke(sectorId: String): Flow<AssetSector?> = repository.getSectorById(sectorId)
}

class SaveSectorUseCase(private val repository: AssetMetadataRepository) {
    suspend operator fun invoke(name: String, icon: String): Result<Unit> {
        val sector = AssetSector(
            id = "sector_${nowMillis()}_${(0..9999).random()}",
            name = name.trim(),
            icon = icon,
            createdAt = nowMillis()
        )
        return repository.saveSector(sector)
    }
}

class DeleteSectorUseCase(private val repository: AssetMetadataRepository) {
    suspend operator fun invoke(sectorId: String): Result<Unit> = repository.deleteSector(sectorId)
}

class GetRegionsUseCase(private val repository: AssetMetadataRepository) {
    operator fun invoke(): Flow<List<AssetRegion>> = repository.getAllRegions()
}

class GetRegionByIdUseCase(private val repository: AssetMetadataRepository) {
    operator fun invoke(regionId: String): Flow<AssetRegion?> = repository.getRegionById(regionId)
}

class SaveRegionUseCase(private val repository: AssetMetadataRepository) {
    suspend operator fun invoke(name: String): Result<Unit> {
        val region = AssetRegion(
            id = "region_${nowMillis()}_${(0..9999).random()}",
            name = name.trim(),
            createdAt = nowMillis()
        )
        return repository.saveRegion(region)
    }
}

class DeleteRegionUseCase(private val repository: AssetMetadataRepository) {
    suspend operator fun invoke(regionId: String): Result<Unit> = repository.deleteRegion(regionId)
}

// ── Sector relations ──────────────────────────────────────────────────────────

class GetSectorsByAssetUseCase(private val repository: AssetMetadataRepository) {
    operator fun invoke(assetId: String): Flow<List<AssetSector>> =
        repository.getSectorsByAssetId(assetId)
}

class GetSectorsByAssetsUseCase(private val repository: AssetMetadataRepository) {
    operator fun invoke(assetIds: List<String>): Flow<List<AssetSectorRelation>> =
        repository.getSectorsByAssetIds(assetIds)
}

class SaveSectorRelationUseCase(private val repository: AssetMetadataRepository) {
    suspend operator fun invoke(relation: AssetSectorRelation): Result<Unit> =
        repository.saveSectorRelation(relation)
}

class DeleteAllSectorLinksUseCase(private val repository: AssetMetadataRepository) {
    suspend operator fun invoke(assetId: String): Result<Unit> =
        repository.deleteAllSectorLinks(assetId)
}

// ── Region distributions ──────────────────────────────────────────────────────

class GetRegionsByAssetUseCase(private val repository: AssetMetadataRepository) {
    operator fun invoke(assetId: String): Flow<List<AssetRegionDistribution>> =
        repository.getRegionDistributionsByAssetId(assetId)
}

class GetRegionsByAssetsUseCase(private val repository: AssetMetadataRepository) {
    operator fun invoke(assetIds: List<String>): Flow<List<AssetRegionDistribution>> =
        repository.getRegionDistributionsByAssetIds(assetIds)
}

class SaveRegionDistributionUseCase(private val repository: AssetMetadataRepository) {
    suspend operator fun invoke(distribution: AssetRegionDistribution): Result<Unit> =
        repository.saveRegionDistribution(distribution)
}

class DeleteAllRegionDistributionsUseCase(private val repository: AssetMetadataRepository) {
    suspend operator fun invoke(assetId: String): Result<Unit> =
        repository.deleteAllRegionDistributions(assetId)
}
