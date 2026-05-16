package es.aviferdev.n3to.domain.usecase.backup

import es.aviferdev.n3to.core.security.AppSettings
import kotlinx.datetime.Clock

/**
 * Guarda la fecha (epoch millis) del último backup export exitoso.
 * Se llama desde BackupViewModel al completar una exportación.
 */
class SaveLastBackupDateUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke() {
        val now = Clock.System.now().toEpochMilliseconds()
        appSettings.putLong(ShouldShowBackupReminderUseCase.KEY_LAST_BACKUP_DATE, now)
    }
}
