package es.aviferdev.n3to.domain.usecase.account

import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.repository.AccountRepository

class SaveAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: Account): Result<Unit> =
        repository.saveAccount(account)
}
