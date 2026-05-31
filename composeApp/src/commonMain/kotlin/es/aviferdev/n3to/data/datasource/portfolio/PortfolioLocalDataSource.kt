package es.aviferdev.n3to.data.datasource.portfolio

import es.aviferdev.n3to.domain.model.Portfolio
import kotlinx.coroutines.flow.Flow

interface PortfolioLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<Portfolio>>
    fun getArchivedByAccount(accountId: String): Flow<List<Portfolio>>
    fun getById(id: String): Flow<Portfolio?>
    fun countByAccount(accountId: String): Flow<Long>
    suspend fun insert(entity: Portfolio)
    suspend fun update(entity: Portfolio)
    suspend fun archive(id: String)
    suspend fun unarchive(id: String)
    suspend fun delete(id: String)
}
