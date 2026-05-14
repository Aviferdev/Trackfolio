package es.aviferdev.trackfolio.domain.usecase.reconciliation

import es.aviferdev.trackfolio.core.security.AppSettings

/**
 * Lee y escribe el intervalo (en días) del recordatorio de reconciliación.
 * Opciones válidas: 0 (desactivado), 7, 15, 30.
 */
class GetReconciliationReminderIntervalUseCase(
    private val appSettings: AppSettings
) {
    fun get(): Int {
        val value = appSettings.getInt(
            ShouldShowReconciliationReminderUseCase.KEY_RECONCILIATION_INTERVAL,
            ShouldShowReconciliationReminderUseCase.DEFAULT_INTERVAL
        )
        return if (value in ShouldShowReconciliationReminderUseCase.VALID_INTERVALS) value
        else ShouldShowReconciliationReminderUseCase.DEFAULT_INTERVAL
    }

    fun set(days: Int) {
        require(days in ShouldShowReconciliationReminderUseCase.VALID_INTERVALS) {
            "Intervalo no válido: $days. Usa 0, 7, 15 o 30."
        }
        appSettings.putInt(
            ShouldShowReconciliationReminderUseCase.KEY_RECONCILIATION_INTERVAL,
            days
        )
    }
}
