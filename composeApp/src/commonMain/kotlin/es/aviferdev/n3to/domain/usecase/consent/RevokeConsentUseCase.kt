package es.aviferdev.n3to.domain.usecase.consent

import es.aviferdev.n3to.domain.repository.ConsentRepository

class RevokeConsentUseCase(
    private val repository: ConsentRepository
) {
    suspend operator fun invoke() {
        repository.revokeAll()
    }
}
