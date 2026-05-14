package es.aviferdev.trackfolio.domain.usecase.backup

import es.aviferdev.trackfolio.core.security.AppSettings

/**
 * Guarda el intervalo (en días) del recordatorio de backup.
 * Se usa desde el diálogo del banner (7, 15, 30) o desde Ajustes (0, 7, 15, 30).
 */
class SaveBackupReminderIntervalUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke(intervalDays: Int) {
        require(intervalDays in ShouldShowBackupReminderUseCase.VALID_INTERVALS) {
            "Intervalo no válido: $intervalDays. Usa 0, 7, 15 o 30."
        }
        appSettings.putInt(
            ShouldShowBackupReminderUseCase.KEY_BACKUP_REMINDER_INTERVAL,
            intervalDays
        )
    }
}
