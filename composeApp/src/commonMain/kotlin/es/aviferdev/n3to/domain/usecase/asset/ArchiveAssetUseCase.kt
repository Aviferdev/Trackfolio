package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.repository.AssetRepository

/**
 * Archiva un activo. Los activos NUNCA se borran de la base de datos;
 * archivar los oculta de los listados normales pero conserva todas sus
 * transacciones y el P&L histórico.
 */
class ArchiveAssetUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.archiveAsset(id)
}
