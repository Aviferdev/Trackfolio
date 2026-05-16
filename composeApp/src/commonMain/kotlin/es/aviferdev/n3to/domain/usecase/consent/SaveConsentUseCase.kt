package es.aviferdev.n3to.domain.usecase.consent

import es.aviferdev.n3to.domain.model.ConsentPreferences
import es.aviferdev.n3to.domain.repository.ConsentRepository
import kotlinx.datetime.Clock

class SaveConsentUseCase(
    private val repository: ConsentRepository
) {
    suspend operator fun invoke(prefs: ConsentPreferences) {
        val stamped = prefs.copy(
            consentTimestamp = Clock.System.now().toEpochMilliseconds(),
            hasDecided = true,
            consentVersion = ConsentPreferences.CURRENT_CONSENT_VERSION
        )
        repository.saveConsent(stamped)
    }
}
