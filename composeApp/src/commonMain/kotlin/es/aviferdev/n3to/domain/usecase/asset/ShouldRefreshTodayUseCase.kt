package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.core.security.AppSettings
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Controla la frecuencia de actualización diaria de precios y tipo de cambio.
 * Verifica si ya se ejecutó una actualización hoy y registra la última fecha.
 *
 * Usa la fecha local del dispositivo para determinar el "día actual".
 * Compara el epoch millis del inicio del día actual con el último refresco.
 *
 * Diferentes claves en AppSettings permiten controlar distintos tipos de refresh
 * (precios de activos vs tipo de cambio).
 */
class ShouldRefreshTodayUseCase(
    private val appSettings: AppSettings
) {
    companion object {
        const val KEY_PRICE_REFRESH = "last_price_refresh_date"
        const val KEY_EXCHANGE_RATE_REFRESH = "last_exchange_rate_date"
    }

    /**
     * Verifica si se debe refrescar según la clave indicada.
     * @param key Clave en AppSettings a comprobar.
     * @return true si no se ha refrescado hoy, false si ya se hizo hoy.
     */
    operator fun invoke(key: String = KEY_PRICE_REFRESH): Boolean {
        val lastEpoch = appSettings.getLong(key, 0L)
        val todayStart = startOfTodayEpoch()
        return lastEpoch < todayStart
    }

    /**
     * Marca que el refresco se ha completado hoy para la clave indicada.
     */
    fun markRefreshedToday(key: String = KEY_PRICE_REFRESH) {
        appSettings.putLong(key, startOfTodayEpoch())
    }

    /**
     * Calcula el epoch millis del inicio del día actual (00:00:00 local).
     * Construye el string ISO "YYYY-MM-DDT00:00:00Z" para el día local.
     */
    private fun startOfTodayEpoch(): Long {
        val now = Clock.System.now()
        val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val dateStr = localNow.date.toString() // "YYYY-MM-DD"
        val startOfDay = Instant.parse("${dateStr}T00:00:00Z")
        return startOfDay.toEpochMilliseconds()
    }
}
