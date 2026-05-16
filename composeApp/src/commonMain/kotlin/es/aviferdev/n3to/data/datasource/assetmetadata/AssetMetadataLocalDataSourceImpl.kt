package es.aviferdev.n3to.data.datasource.assetmetadata

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import es.aviferdev.n3to.data.database.N3toDatabase
import es.aviferdev.n3to.data.database.mapper.toDomain
import es.aviferdev.n3to.data.database.mapper.toEntity
import es.aviferdev.n3to.domain.model.AssetComposition
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetMetadataLocalDataSourceImpl(
    private val database: N3toDatabase
) : AssetMetadataLocalDataSource {

    private val compQueries = database.assetCompositionQueries
    private val sectorQueries = database.assetSectorQueries
    private val sectorRelQueries = database.assetSectorRelationQueries
    private val regionQueries = database.assetRegionQueries
    private val regionDistQueries = database.assetRegionDistributionQueries

    override fun getCompositionByAsset(assetId: String): Flow<AssetComposition?> =
        compQueries.selectByAsset(assetId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun getAllCompositions(): Flow<List<AssetComposition>> =
        compQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getCompositionsByAssets(assetIds: List<String>): Flow<List<AssetComposition>> =
        compQueries.selectByAssets(assetIds)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun saveComposition(composition: AssetComposition): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = composition.toEntity()
                compQueries.insertOrUpdate(
                    assetId = e.assetId,
                    fixedIncomePercent = e.fixedIncomePercent,
                    createdAt = e.createdAt
                )
            }
        }

    override suspend fun deleteComposition(assetId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { compQueries.delete(assetId) }
        }

    override fun getAllSectors(): Flow<List<AssetSector>> =
        sectorQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getSectorById(sectorId: String): Flow<AssetSector?> =
        sectorQueries.selectById(sectorId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override suspend fun insertSector(sector: AssetSector): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = sector.toEntity()
                sectorQueries.insert(e.id, e.name, e.icon, e.createdAt)
            }
        }

    override suspend fun deleteSector(sectorId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { sectorQueries.delete(sectorId) }
        }

    override fun getSectorsByAsset(assetId: String): Flow<List<AssetSector>> =
        sectorRelQueries.selectByAsset(assetId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { relations ->
                relations.mapNotNull { rel ->
                    sectorQueries.selectById(rel.sectorId)
                        .executeAsOneOrNull()?.toDomain()
                }
            }

    override fun getSectorsByAssets(assetIds: List<String>): Flow<List<AssetSectorRelation>> =
        sectorRelQueries.selectAllByAssets(assetIds)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun linkSector(assetId: String, sectorId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                sectorRelQueries.insert(assetId, sectorId)
            }
        }

    override suspend fun unlinkSector(assetId: String, sectorId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                sectorRelQueries.deleteRelation(assetId, sectorId)
            }
        }

    override suspend fun deleteAllSectorLinks(assetId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                sectorRelQueries.deleteByAsset(assetId)
            }
        }

    override fun getAllRegions(): Flow<List<AssetRegion>> =
        regionQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun insertRegion(region: AssetRegion): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = region.toEntity()
                regionQueries.insert(e.id, e.name, e.createdAt)
            }
        }

    override suspend fun deleteRegion(regionId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) { regionQueries.delete(regionId) }
        }

    override fun getRegionById(regionId: String): Flow<AssetRegion?> =
        regionQueries.selectById(regionId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() }

    override fun getRegionsByAsset(assetId: String): Flow<List<AssetRegionDistribution>> =
        regionDistQueries.selectByAsset(assetId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override fun getRegionsByAssets(assetIds: List<String>): Flow<List<AssetRegionDistribution>> =
        regionDistQueries.selectAllByAssets(assetIds)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun saveRegionDistribution(distribution: AssetRegionDistribution): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val e = distribution.toEntity()
                regionDistQueries.insert(e.assetId, e.regionId, e.percent)
            }
        }

    override suspend fun deleteAllRegionDistributions(assetId: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                regionDistQueries.deleteByAsset(assetId)
            }
        }

    override suspend fun countSectors(): Long =
        withContext(Dispatchers.IO) {
            database.assetSectorQueries.selectAll().executeAsList().size.toLong()
        }

    override suspend fun countRegions(): Long =
        withContext(Dispatchers.IO) {
            database.assetRegionQueries.selectAll().executeAsList().size.toLong()
        }
}