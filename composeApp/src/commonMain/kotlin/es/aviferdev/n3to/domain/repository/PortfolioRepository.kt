package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.Portfolio
import kotlinx.coroutines.flow.Flow

interface PortfolioRepository {
    fun getByAccount(accountId: String): Flow<List<Portfolio>>
    fun getArchivedByAccount(accountId: String): Flow<List<Portfolio>>
    fun getById(id: String): Flow<Portfolio?>
    suspend fun save(portfolio: Portfolio): Result<Unit>
    suspend fun update(portfolio: Portfolio): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
