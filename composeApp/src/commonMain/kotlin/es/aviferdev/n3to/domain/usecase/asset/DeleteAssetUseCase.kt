// DEPRECATED: Los activos NUNCA se borran, solo se archivan.
// Usar ArchiveAssetUseCase en su lugar.
// Se mantiene temporalmente para evitar errores de compilación durante la transición.
package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.repository.AssetRepository

@Deprecated("Los activos no se borran. Usar ArchiveAssetUseCase.", replaceWith = ReplaceWith("ArchiveAssetUseCase"))
class DeleteAssetUseCase(private val repository: AssetRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.archiveAsset(id)
}
