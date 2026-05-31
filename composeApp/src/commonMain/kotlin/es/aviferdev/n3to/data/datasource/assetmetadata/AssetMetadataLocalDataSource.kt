package es.aviferdev.n3to.data.datasource.assetmetadata

import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import kotlinx.coroutines.flow.Flow

interface AssetMetadataLocalDataSource {
    fun getAllSectors(): Flow<List<AssetSector>>
    fun getSectorById(sectorId: String): Flow<AssetSector?>
    suspend fun insertSector(sector: AssetSector): Result<Unit>
    suspend fun deleteSector(sectorId: String): Result<Unit>

    fun getSectorsByAsset(assetId: String): Flow<List<AssetSector>>
    fun getSectorsByAssets(assetIds: List<String>): Flow<List<AssetSectorRelation>>
    suspend fun linkSector(assetId: String, sectorId: String): Result<Unit>
    suspend fun unlinkSector(assetId: String, sectorId: String): Result<Unit>
    suspend fun deleteAllSectorLinks(assetId: String): Result<Unit>

    fun getAllRegions(): Flow<List<AssetRegion>>
    fun getRegionById(regionId: String): Flow<AssetRegion?>
    suspend fun insertRegion(region: AssetRegion): Result<Unit>
    suspend fun deleteRegion(regionId: String): Result<Unit>

    fun getRegionsByAsset(assetId: String): Flow<List<AssetRegionDistribution>>
    fun getRegionsByAssets(assetIds: List<String>): Flow<List<AssetRegionDistribution>>
    suspend fun saveRegionDistribution(distribution: AssetRegionDistribution): Result<Unit>
    suspend fun deleteAllRegionDistributions(assetId: String): Result<Unit>

    suspend fun countSectors(): Long
    suspend fun countRegions(): Long
}
