package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.AssetComposition
import es.aviferdev.trackfolio.domain.model.AssetRegion
import es.aviferdev.trackfolio.domain.model.AssetRegionDistribution
import es.aviferdev.trackfolio.domain.model.AssetSector
import es.aviferdev.trackfolio.domain.model.AssetSectorRelation
import kotlinx.coroutines.flow.Flow

interface AssetMetadataRepository {
    fun getCompositionByAssetId(assetId: String): Flow<AssetComposition?>
    fun getAllCompositions(): Flow<List<AssetComposition>>
    fun getCompositionsByAssetIds(assetIds: List<String>): Flow<List<AssetComposition>>
    fun getAllSectors(): Flow<List<AssetSector>>
    fun getSectorById(id: String): Flow<AssetSector?>
    fun getSectorsByAssetId(assetId: String): Flow<List<AssetSector>>
    fun getSectorsByAssetIds(assetIds: List<String>): Flow<List<AssetSectorRelation>>
    fun getAllRegions(): Flow<List<AssetRegion>>
    fun getRegionById(id: String): Flow<AssetRegion?>
    fun getRegionDistributionsByAssetId(assetId: String): Flow<List<AssetRegionDistribution>>
    fun getRegionDistributionsByAssetIds(assetIds: List<String>): Flow<List<AssetRegionDistribution>>

    suspend fun saveComposition(composition: AssetComposition): Result<Unit>
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