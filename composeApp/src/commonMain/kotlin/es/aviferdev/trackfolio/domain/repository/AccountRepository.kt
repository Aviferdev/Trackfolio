package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAll(): Flow<List<Account>>
    fun getById(id: String): Flow<Account?>
    fun getTotalBalance(): Flow<Double>
    suspend fun save(account: Account): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
