package es.aviferdev.n3to.domain.usecase.asset

import kotlin.math.abs

/**
 * Detecta cambios anómalos en el precio de un activo.
 *
 * Proporciona dos niveles de alerta:
 * - [Warning]: Variación ≥ 50% desde el último precio conocido.
 * - [Suspicious]: Variación ≥ 500% (5x o más) en entrada manual, con
 *   sugerencia de posible error tipográfico (coma decimal mal puesta).
 */
class DetectPriceAnomalyUseCase {

    companion object {
        /** Umbral para advertencia (50% de cambio) */
        const val WARNING_THRESHOLD_PERCENT = 50.0
        /** Umbral para bloqueo/sospecha en entrada manual (500% = 5x) */
        const val SUSPICIOUS_THRESHOLD_PERCENT = 500.0
    }

    /**
     * Resultado de la detección de anomalías.
     */
    sealed class Result {
        /** Precio normal, sin anomalías detectadas. */
        data object Normal : Result()

        /** Variación significativa pero plausible. */
        data class Warning(
            val previousPrice: Double,
            val newPrice: Double,
            val percentChange: Double,
            val isManualEntry: Boolean
        ) : Result()

        /** Variación extremadamente sospechosa (probable error del usuario). */
        data class Suspicious(
            val previousPrice: Double,
            val newPrice: Double,
            val percentChange: Double,
            val likelyCause: String
        ) : Result()
    }

    /**
     * Evalúa si el nuevo precio presenta una anomalía respecto al anterior.
     * @param previousPrice Precio anterior (puede ser null si no hay histórico).
     * @param newPrice Nuevo precio introducido.
     * @param isManualEntry true si el precio fue introducido por el usuario
     *                      (no por la API automática).
     * @return Result de la clasificación.
     */
    operator fun invoke(
        previousPrice: Double?,
        newPrice: Double,
        isManualEntry: Boolean
    ): Result {
        // Si no hay precio anterior o es cero, no se puede comparar
        if (previousPrice == null || previousPrice == 0.0) {
            return Result.Normal
        }

        val change = ((newPrice - previousPrice) / previousPrice) * 100.0
        val absChange = abs(change)

        return when {
            // Solo para entrada manual: detectar posibles errores de coma decimal
            absChange >= SUSPICIOUS_THRESHOLD_PERCENT && isManualEntry -> {
                val factor = if (previousPrice != 0.0) newPrice / previousPrice else 0.0
                val changeStr = "${absChange.toInt()}%"
                val cause = when {
                    factor in 0.09..0.11 || factor in 9.0..11.0 ->
                        "Posible error de coma decimal (factor 10x)"
                    factor in 0.009..0.011 || factor in 99.0..101.0 ->
                        "Posible error de coma decimal (factor 100x)"
                    factor > 1000 ->
                        "Variación extremadamente alta ($changeStr)"
                    else ->
                        "Variación inusualmente alta ($changeStr)"
                }
                Result.Suspicious(previousPrice, newPrice, change, cause)
            }
            // Advertencia para cambios significativos (tanto manual como auto)
            absChange >= WARNING_THRESHOLD_PERCENT ->
                Result.Warning(previousPrice, newPrice, change, isManualEntry)
            // Normal
            else -> Result.Normal
        }
    }
}
