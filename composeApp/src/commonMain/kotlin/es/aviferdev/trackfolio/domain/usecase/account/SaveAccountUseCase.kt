package es.aviferdev.trackfolio.domain.usecase.account

import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.repository.AccountRepository

class SaveAccountUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(account: Account): Result<Unit> = repository.save(account)
}
