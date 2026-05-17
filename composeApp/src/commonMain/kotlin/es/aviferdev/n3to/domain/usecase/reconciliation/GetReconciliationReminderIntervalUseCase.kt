package es.aviferdev.n3to.domain.usecase.reconciliation

import es.aviferdev.n3to.core.security.AppSettings

/**
 * Lee y escribe el intervalo (en días) del recordatorio de reconciliación
 * para una cuenta concreta.
 * Opciones válidas: 0 (desactivado), 7, 15, 30.
 */
class GetReconciliationReminderIntervalUseCase(
    private val appSettings: AppSettings
) {
    fun get(accountId: String): Int {
        val value = appSettings.getInt(
            ShouldShowReconciliationReminderUseCase.keyInterval(accountId),
            ShouldShowReconciliationReminderUseCase.DEFAULT_INTERVAL
        )
        return if (value in ShouldShowReconciliationReminderUseCase.VALID_INTERVALS) value
        else ShouldShowReconciliationReminderUseCase.DEFAULT_INTERVAL
    }

    fun set(accountId: String, days: Int) {
        require(days in ShouldShowReconciliationReminderUseCase.VALID_INTERVALS) {
            "Intervalo no válido: $days. Usa 0, 7, 15 o 30."
        }
        appSettings.putInt(
            ShouldShowReconciliationReminderUseCase.keyInterval(accountId),
            days
        )
    }
}
