package es.aviferdev.trackfolio.domain.usecase.account

import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAccountByIdUseCase(private val repository: AccountRepository) {
    operator fun invoke(id: String): Flow<Account?> = repository.getAccountById(id)
}
