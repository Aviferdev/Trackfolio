package es.aviferdev.n3to.data.datasource.portfolio

import es.aviferdev.n3to.data.database.PortfolioEntity
import kotlinx.coroutines.flow.Flow

interface PortfolioLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<PortfolioEntity>>
    fun getArchivedByAccount(accountId: String): Flow<List<PortfolioEntity>>
    fun getById(id: String): Flow<PortfolioEntity?>
    fun countByAccount(accountId: String): Flow<Long>
    suspend fun insert(entity: PortfolioEntity)
    suspend fun update(entity: PortfolioEntity)
    suspend fun archive(id: String)
    suspend fun unarchive(id: String)
    suspend fun delete(id: String)
}
