package es.aviferdev.trackfolio.domain.usecase.asset

import es.aviferdev.trackfolio.core.security.AppSettings

/**
 * Lee y escribe el intervalo (en días) del recordatorio de precios.
 * Opciones válidas: 7, 14, 30. Cualquier otro valor se trata como 14.
 */
class GetPriceReminderIntervalUseCase(
    private val appSettings: AppSettings
) {
    fun get(): Int {
        val value = appSettings.getInt(
            GetOutdatedAssetsUseCase.KEY_REMINDER_INTERVAL,
            GetOutdatedAssetsUseCase.DEFAULT_INTERVAL
        )
        return if (value in VALID_INTERVALS) value else GetOutdatedAssetsUseCase.DEFAULT_INTERVAL
    }

    fun set(days: Int) {
        require(days in VALID_INTERVALS) { "Intervalo no válido: $days. Usa 7, 14 o 30." }
        appSettings.putInt(GetOutdatedAssetsUseCase.KEY_REMINDER_INTERVAL, days)
    }

    companion object {
        val VALID_INTERVALS = listOf(7, 14, 30)
    }
}
