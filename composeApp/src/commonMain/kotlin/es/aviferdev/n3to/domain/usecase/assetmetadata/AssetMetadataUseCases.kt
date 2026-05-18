package es.aviferdev.n3to.domain.usecase.assetmetadata

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetSector
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