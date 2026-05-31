package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import kotlinx.coroutines.flow.Flow

interface AssetMetadataRepository {
    fun getAllSectors(): Flow<List<AssetSector>>
    fun getSectorById(id: String): Flow<AssetSector?>
    fun getSectorsByAssetId(assetId: String): Flow<List<AssetSector>>
    fun getSectorsByAssetIds(assetIds: List<String>): Flow<List<AssetSectorRelation>>
    fun getAllRegions(): Flow<List<AssetRegion>>
    fun getRegionById(id: String): Flow<AssetRegion?>
    fun getRegionDistributionsByAssetId(assetId: String): Flow<List<AssetRegionDistribution>>
    fun getRegionDistributionsByAssetIds(assetIds: List<String>): Flow<List<AssetRegionDistribution>>

    suspend fun saveSector(sector: AssetSector): Result<Unit>
    suspend fun deleteSector(sectorId: String): Result<Unit>
    suspend fun saveSectorRelation(relation: AssetSectorRelation): Result<Unit>
    suspend fun deleteSectorRelation(assetId: String, sectorId: String): Result<Unit>
    suspend fun deleteAllSectorLinks(assetId: String): Result<Unit>
    suspend fun saveRegion(region: AssetRegion): Result<Unit>
    suspend fun deleteRegion(regionId: String): Result<Unit>
    suspend fun saveRegionDistribution(distribution: AssetRegionDistribution): Result<Unit>
    suspend fun deleteAllRegionDistributions(assetId: String): Result<Unit>
}
