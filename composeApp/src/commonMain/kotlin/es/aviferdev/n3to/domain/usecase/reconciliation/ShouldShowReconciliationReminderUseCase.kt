package es.aviferdev.n3to.domain.usecase.reconciliation

import es.aviferdev.n3to.core.security.AppSettings
import kotlinx.datetime.Clock

/**
 * Determina si se debe mostrar el banner de recordatorio de reconciliación.
 * Comprueba:
 * 1. Que el recordatorio esté habilitado (intervalo > 0).
 * 2. Que haya pasado al menos [intervalo] días desde la última reconciliación.
 */
class ShouldShowReconciliationReminderUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke(): Boolean {
        val intervalDays = appSettings.getInt(KEY_RECONCILIATION_INTERVAL, DEFAULT_INTERVAL)
        if (intervalDays <= 0) return false

        val lastReconciliation = appSettings.getLong(
            ReconcileBalanceUseCase.KEY_LAST_RECONCILIATION_DATE, 0L
        )
        val now = Clock.System.now().toEpochMilliseconds()
        val intervalMillis = intervalDays.toLong() * 24 * 60 * 60 * 1000

        return (now - lastReconciliation) >= intervalMillis
    }

    companion object {
        const val KEY_RECONCILIATION_INTERVAL = "reconciliation_reminder_interval_days"
        const val DEFAULT_INTERVAL = 0 // Desactivado por defecto
        val VALID_INTERVALS = listOf(0, 7, 15, 30) // 0 = desactivado
    }
}
