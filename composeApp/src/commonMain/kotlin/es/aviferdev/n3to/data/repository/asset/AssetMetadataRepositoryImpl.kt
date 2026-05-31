package es.aviferdev.n3to.data.repository.asset

import es.aviferdev.n3to.data.datasource.assetmetadata.AssetMetadataLocalDataSource
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import es.aviferdev.n3to.domain.repository.AssetMetadataRepository
import kotlinx.coroutines.flow.Flow

class AssetMetadataRepositoryImpl(
    private val localDataSource: AssetMetadataLocalDataSource
) : AssetMetadataRepository {

    override fun getAllSectors(): Flow<List<AssetSector>> =
        localDataSource.getAllSectors()

    override fun getSectorById(id: String): Flow<AssetSector?> =
        localDataSource.getSectorById(id)

    override fun getSectorsByAssetId(assetId: String): Flow<List<AssetSector>> =
        localDataSource.getSectorsByAsset(assetId)

    override fun getSectorsByAssetIds(assetIds: List<String>): Flow<List<AssetSectorRelation>> =
        localDataSource.getSectorsByAssets(assetIds)

    override fun getAllRegions(): Flow<List<AssetRegion>> =
        localDataSource.getAllRegions()

    override fun getRegionById(id: String): Flow<AssetRegion?> =
        localDataSource.getRegionById(id)

    override fun getRegionDistributionsByAssetId(assetId: String): Flow<List<AssetRegionDistribution>> =
        localDataSource.getRegionsByAsset(assetId)

    override fun getRegionDistributionsByAssetIds(assetIds: List<String>): Flow<List<AssetRegionDistribution>> =
        localDataSource.getRegionsByAssets(assetIds)

    override suspend fun saveSector(sector: AssetSector): Result<Unit> =
        localDataSource.insertSector(sector)

    override suspend fun deleteSector(sectorId: String): Result<Unit> =
        localDataSource.deleteSector(sectorId)

    override suspend fun saveSectorRelation(relation: AssetSectorRelation): Result<Unit> =
        localDataSource.linkSector(relation.assetId, relation.sectorId)

    override suspend fun deleteSectorRelation(assetId: String, sectorId: String): Result<Unit> =
        localDataSource.unlinkSector(assetId, sectorId)

    override suspend fun deleteAllSectorLinks(assetId: String): Result<Unit> =
        localDataSource.deleteAllSectorLinks(assetId)

    override suspend fun saveRegion(region: AssetRegion): Result<Unit> =
        localDataSource.insertRegion(region)

    override suspend fun deleteRegion(regionId: String): Result<Unit> =
        localDataSource.deleteRegion(regionId)

    override suspend fun saveRegionDistribution(distribution: AssetRegionDistribution): Result<Unit> =
        localDataSource.saveRegionDistribution(distribution)

    override suspend fun deleteAllRegionDistributions(assetId: String): Result<Unit> =
        localDataSource.deleteAllRegionDistributions(assetId)
}
