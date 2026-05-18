package es.aviferdev.n3to.domain.usecase.backup

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.domain.model.ValidationError

/**
 * Guarda el intervalo (en días) del recordatorio de backup.
 * Se usa desde el diálogo del banner (7, 15, 30) o desde Ajustes (0, 7, 15, 30).
 */
class SaveBackupReminderIntervalUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke(intervalDays: Int): Result<Unit> {
        if (intervalDays !in ShouldShowBackupReminderUseCase.VALID_INTERVALS)
            return Result.failure(ValidationError.InvalidInterval(intervalDays))
        appSettings.putInt(
            ShouldShowBackupReminderUseCase.KEY_BACKUP_REMINDER_INTERVAL,
            intervalDays
        )
        return Result.success(Unit)
    }
}
