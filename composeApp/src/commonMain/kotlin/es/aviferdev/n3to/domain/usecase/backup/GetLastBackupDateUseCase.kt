package es.aviferdev.n3to.domain.usecase.backup

import es.aviferdev.n3to.core.security.AppSettings

/**
 * Devuelve la fecha (epoch millis) del último backup export exitoso.
 * Retorna 0L si nunca se ha hecho un backup.
 */
class GetLastBackupDateUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke(): Long {
        return appSettings.getLong(
            ShouldShowBackupReminderUseCase.KEY_LAST_BACKUP_DATE,
            default = 0L
        )
    }
}
