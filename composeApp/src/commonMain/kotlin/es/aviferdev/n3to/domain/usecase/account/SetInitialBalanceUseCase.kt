package es.aviferdev.n3to.domain.usecase.account

import es.aviferdev.n3to.domain.repository.AccountRepository

class SetInitialBalanceUseCase(private val repository: AccountRepository) {
    suspend operator fun invoke(accountId: String, amount: Double): Result<Unit> =
        repository.setInitialBalance(accountId, amount)
}
