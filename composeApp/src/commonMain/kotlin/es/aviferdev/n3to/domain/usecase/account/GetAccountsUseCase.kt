package es.aviferdev.n3to.domain.usecase.account

import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAccountsUseCase(private val repository: AccountRepository) {
    operator fun invoke(): Flow<List<Account>> = repository.getAllAccounts()
}
