package es.aviferdev.n3to.domain.usecase.consent

import es.aviferdev.n3to.domain.model.ConsentPreferences
import es.aviferdev.n3to.domain.repository.ConsentRepository

class HasUserDecidedUseCase(
    private val repository: ConsentRepository
) {
    suspend operator fun invoke(): Boolean {
        val prefs = repository.getConsent()
        return prefs.hasDecided &&
                prefs.consentVersion >= ConsentPreferences.CURRENT_CONSENT_VERSION
    }
}
