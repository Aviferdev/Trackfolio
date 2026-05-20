package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.platform.nowLocalDate
import kotlinx.datetime.Instant

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

    private fun startOfTodayEpoch(): Long {
        val dateStr = nowLocalDate().toString()
        return Instant.parse("${dateStr}T00:00:00Z").toEpochMilliseconds()
    }
}
