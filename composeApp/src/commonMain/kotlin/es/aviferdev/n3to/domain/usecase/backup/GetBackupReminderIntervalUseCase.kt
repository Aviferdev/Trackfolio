package es.aviferdev.n3to.domain.usecase.backup

import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.domain.model.ValidationError

/**
 * Lee y escribe el intervalo (en días) del recordatorio de backup.
 *
 * Opciones válidas desde el diálogo del banner: 7, 15, 30.
 * Opción 0 (desactivado) solo se puede establecer desde Ajustes.
 */
class GetBackupReminderIntervalUseCase(
    private val appSettings: AppSettings
) {
    fun get(): Int {
        val value = appSettings.getInt(
            ShouldShowBackupReminderUseCase.KEY_BACKUP_REMINDER_INTERVAL,
            ShouldShowBackupReminderUseCase.DEFAULT_INTERVAL_DAYS
        )
        return if (value in ShouldShowBackupReminderUseCase.VALID_INTERVALS) value
        else ShouldShowBackupReminderUseCase.DEFAULT_INTERVAL_DAYS
    }

    fun set(days: Int): Result<Unit> {
        if (days !in ShouldShowBackupReminderUseCase.VALID_INTERVALS)
            return Result.failure(ValidationError.InvalidInterval(days))
        appSettings.putInt(
            ShouldShowBackupReminderUseCase.KEY_BACKUP_REMINDER_INTERVAL,
            days
        )
        return Result.success(Unit)
    }
}
