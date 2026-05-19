package es.aviferdev.n3to.domain.model

/**
 * Resultado de la operación de refresco diario de precios de cartera.
 *
 * @property totalQuotableAssets Número total de activos cotizables con ISIN.
 * @property updatedCount Activos cuyo precio se actualizó correctamente.
 * @property failedCount Activos en los que la API falló (error de red/timeout).
 * @property notFoundIdentifiers Lista de ISINs que no se encontraron en la API.
 * @property skippedCount Activos sin ISIN o de categorías no cotizables.
 * @property exchangeRateUsed Tipo de cambio EUR/USD usado para la conversión (puede ser null si no se obtuvo).
 * @property convertedCount Cuántos precios se convirtieron de USD a EUR.
 */
data class PriceRefreshResult(
    val totalQuotableAssets: Int = 0,
    val updatedCount: Int = 0,
    val failedCount: Int = 0,
    val notFoundIdentifiers: List<String> = emptyList(),
    val skippedCount: Int = 0,
    val exchangeRateUsed: ExchangeRate? = null,
    val convertedCount: Int = 0
) {
    /** True si al menos un activo se actualizó correctamente. */
    val hasUpdates: Boolean get() = updatedCount > 0

    /** True si algún ISIN no se encontró en la API. */
    val hasNotFound: Boolean get() = notFoundIdentifiers.isNotEmpty()

    /** True si hubo errores de red/timeout. */
    val hasFailures: Boolean get() = failedCount > 0

    /** Mensaje resumen para mostrar al usuario. */
    val summary: String
        get() {
            val parts = mutableListOf<String>()
            if (updatedCount > 0) parts.add("$updatedCount actualizados")
            if (convertedCount > 0) parts.add("$convertedCount convertidos")
            if (failedCount > 0) parts.add("$failedCount con error")
            if (notFoundIdentifiers.isNotEmpty()) parts.add("${notFoundIdentifiers.size} no encontrados")
            if (skippedCount > 0) parts.add("$skippedCount omitidos")
            return if (parts.isEmpty()) "Sin cambios" else parts.joinToString(", ")
        }
}
