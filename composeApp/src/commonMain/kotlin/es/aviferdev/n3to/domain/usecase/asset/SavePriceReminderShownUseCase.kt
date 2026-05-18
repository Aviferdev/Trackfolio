package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.core.security.AppSettings

/**
 * Registra que el recordatorio de precios ha sido completado
 * (todos los activos actualizados). Actualiza el timestamp
 * para que no vuelva a aparecer hasta que pase el intervalo.
 */
class SavePriceReminderShownUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke() {
        val now = nowMillis()
        appSettings.putLong(ShouldShowPriceReminderUseCase.KEY_LAST_REMINDER_SHOWN, now)
    }
}
