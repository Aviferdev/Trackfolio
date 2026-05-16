package es.aviferdev.n3to.domain.usecase.consent

import es.aviferdev.n3to.domain.model.ConsentPreferences
import es.aviferdev.n3to.domain.repository.ConsentRepository

class GetConsentUseCase(
    private val repository: ConsentRepository
) {
    suspend operator fun invoke(): ConsentPreferences = repository.getConsent()
}
