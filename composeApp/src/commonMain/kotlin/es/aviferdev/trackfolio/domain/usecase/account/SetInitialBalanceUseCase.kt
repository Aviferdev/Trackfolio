package es.aviferdev.trackfolio.domain.usecase.account

import es.aviferdev.trackfolio.domain.repository.AccountRepository

class SetInitialBalanceUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(accountId: String, amount: Double): Result<Unit> =
        repository.setInitialBalance(accountId, amount)
}
