package es.aviferdev.trackfolio.domain.usecase.asset

import es.aviferdev.trackfolio.domain.repository.AssetRepository

/**
 * Desarchiva un activo previamente archivado, devolviéndolo a los
 * listados activos.
 */
class UnarchiveAssetUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.unarchiveAsset(id)
}
