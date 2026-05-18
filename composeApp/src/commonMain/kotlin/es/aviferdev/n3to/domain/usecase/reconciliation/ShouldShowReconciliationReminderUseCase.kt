package es.aviferdev.n3to.domain.usecase.reconciliation

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.core.security.AppSettings

/**
 * Determina si se debe mostrar el banner de recordatorio de reconciliación
 * para una cuenta concreta.
 * Comprueba:
 * 1. Que el recordatorio esté habilitado (intervalo > 0) para esa cuenta.
 * 2. Que haya pasado al menos [intervalo] días desde la última reconciliación.
 */
class ShouldShowReconciliationReminderUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke(accountId: String): Boolean {
        val intervalDays = appSettings.getInt(keyInterval(accountId), DEFAULT_INTERVAL)
        if (intervalDays <= 0) return false

        val lastReconciliation = appSettings.getLong(
            ReconcileBalanceUseCase.keyLastDate(accountId), 0L
        )
        val now = nowMillis()
        val intervalMillis = intervalDays.toLong() * 24 * 60 * 60 * 1000

        return (now - lastReconciliation) >= intervalMillis
    }

    companion object {
        const val DEFAULT_INTERVAL = 0 // Desactivado por defecto
        val VALID_INTERVALS = listOf(0, 7, 15, 30) // 0 = desactivado

        fun keyInterval(accountId: String) = "reconciliation_interval_$accountId"
    }
}
