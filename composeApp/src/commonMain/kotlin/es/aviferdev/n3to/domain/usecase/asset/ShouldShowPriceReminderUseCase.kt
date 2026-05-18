package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.core.security.AppSettings

/**
 * Determina si se debe mostrar el banner de recordatorio de precios.
 * Comprueba:
 * 1. Que el recordatorio esté habilitado (intervalo > 0).
 * 2. Que haya pasado al menos [intervalo] días desde la última vez
 *    que se mostró el recordatorio.
 */
class ShouldShowPriceReminderUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke(): Boolean {
        val intervalDays = appSettings.getInt(
            GetOutdatedAssetsUseCase.KEY_REMINDER_INTERVAL,
            GetOutdatedAssetsUseCase.DEFAULT_INTERVAL
        )
        if (intervalDays <= 0) return false

        val lastShown = appSettings.getLong(KEY_LAST_REMINDER_SHOWN, 0L)
        val now = nowMillis()
        val intervalMillis = intervalDays.toLong() * 24 * 60 * 60 * 1000

        return (now - lastShown) >= intervalMillis
    }

    companion object {
        const val KEY_LAST_REMINDER_SHOWN = "last_price_reminder_shown_at"
    }
}
