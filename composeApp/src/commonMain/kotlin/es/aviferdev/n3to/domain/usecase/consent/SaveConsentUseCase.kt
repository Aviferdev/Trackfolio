package es.aviferdev.n3to.domain.usecase.consent

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.ConsentPreferences
import es.aviferdev.n3to.domain.repository.ConsentRepository

class SaveConsentUseCase(
    private val repository: ConsentRepository
) {
    suspend operator fun invoke(prefs: ConsentPreferences) {
        val stamped = prefs.copy(
            consentTimestamp = nowMillis(),
            hasDecided = true,
            consentVersion = ConsentPreferences.CURRENT_CONSENT_VERSION
        )
        repository.saveConsent(stamped)
    }
}
