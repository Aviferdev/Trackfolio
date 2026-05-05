package es.aviferdev.trackfolio.domain.usecase.asset

import es.aviferdev.trackfolio.domain.repository.AssetRepository

/**
 * Actualiza únicamente el precio actual y la fecha de última actualización
 * de un activo existente. Pensado para flujos rápidos desde la pantalla de
 * Portfolio sin tocar el resto de campos.
 *
 * Cuando exista una API de cotizaciones (Sprint 12+), este caso de uso se
 * podrá invocar también desde un job de fondo.
 */
class UpdateAssetCurrentPriceUseCase(
    private val repository: AssetRepository
) {
    suspend operator fun invoke(
        assetId: String,
        price: Double,
        updatedAt: Long
    ): Result<Unit> = repository.updateCurrentPrice(assetId, price, updatedAt)
}
