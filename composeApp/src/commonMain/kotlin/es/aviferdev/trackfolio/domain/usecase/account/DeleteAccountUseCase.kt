package es.aviferdev.trackfolio.domain.usecase.account

import es.aviferdev.trackfolio.domain.repository.AccountRepository

class DeleteAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.delete(id)
}
