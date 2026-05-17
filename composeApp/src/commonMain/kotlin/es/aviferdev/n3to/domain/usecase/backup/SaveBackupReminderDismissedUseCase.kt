package es.aviferdev.n3to.domain.usecase.backup

import es.aviferdev.n3to.core.security.AppSettings
import kotlinx.datetime.Clock

class SaveBackupReminderDismissedUseCase(
    private val appSettings: AppSettings
) {
    operator fun invoke() {
        appSettings.putLong(
            ShouldShowBackupReminderUseCase.KEY_LAST_BANNER_DISMISSED_DATE,
            Clock.System.now().toEpochMilliseconds()
        )
    }
}
