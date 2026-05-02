package es.aviferdev.trackfolio.domain.usecase.account

import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAccountsUseCase(private val repository: AccountRepository) {
    operator fun invoke(): Flow<List<Account>> = repository.getAll()
}
