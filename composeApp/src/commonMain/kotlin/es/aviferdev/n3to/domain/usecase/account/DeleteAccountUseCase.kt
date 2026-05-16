package es.aviferdev.n3to.domain.usecase.account

import es.aviferdev.n3to.domain.repository.AccountRepository

class DeleteAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(accountId: String): Result<Unit> =
        repository.deleteAccount(accountId)
}
