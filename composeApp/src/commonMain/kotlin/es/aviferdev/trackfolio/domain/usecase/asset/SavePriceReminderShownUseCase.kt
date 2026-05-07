package es.aviferdev.trackfolio.domain.usecase.asset

import es.aviferdev.trackfolio.security.AppSettings
import kotlinx.datetime.Clock

/**
 * Registra que el recordatorio de precios ha sido completado
 * (todos los activos actualizados). Actualiza el timestamp
 * para que no vuelva a aparecer hasta que pase el intervalo.
 */
class SavePriceReminderShownUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke() {
        val now = Clock.System.now().toEpochMilliseconds()
        appSettings.putLong(ShouldShowPriceReminderUseCase.KEY_LAST_REMINDER_SHOWN, now)
    }
}
