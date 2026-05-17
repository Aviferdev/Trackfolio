package es.aviferdev.n3to.domain.usecase.backup

import es.aviferdev.n3to.core.security.AppSettings
import kotlinx.datetime.Clock

/**
 * Determina si se debe mostrar el banner de recordatorio de backup.
 *
 * Comprueba:
 * 1. Que el recordatorio esté habilitado (intervalo > 0).
 * 2. Que haya pasado al menos [intervalo] días desde el último backup.
 * 3. Si nunca se ha hecho backup (lastBackupDate == 0), se muestra el banner.
 */
class ShouldShowBackupReminderUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke(): Boolean {
        val intervalDays = appSettings.getInt(KEY_BACKUP_REMINDER_INTERVAL, DEFAULT_INTERVAL_DAYS)
        if (intervalDays <= 0) return false

        val lastBackup = appSettings.getLong(KEY_LAST_BACKUP_DATE, 0L)
        val lastDismissed = appSettings.getLong(KEY_LAST_BANNER_DISMISSED_DATE, 0L)
        val referenceDate = maxOf(lastBackup, lastDismissed)

        // Nunca ha hecho backup ni descartado el banner → mostrar
        if (referenceDate == 0L) return true

        val now = Clock.System.now().toEpochMilliseconds()
        val intervalMillis = intervalDays.toLong() * 24 * 60 * 60 * 1000

        return (now - referenceDate) >= intervalMillis
    }

    companion object {
        const val KEY_LAST_BACKUP_DATE = "last_backup_date"
        const val KEY_LAST_BANNER_DISMISSED_DATE = "backup_reminder_dismissed_date"
        const val KEY_BACKUP_REMINDER_INTERVAL = "backup_reminder_interval_days"
        const val DEFAULT_INTERVAL_DAYS = 30
        val VALID_INTERVALS = listOf(0, 7, 15, 30) // 0 = desactivado
    }
}
