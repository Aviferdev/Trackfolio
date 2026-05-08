package es.aviferdev.trackfolio.data.datasource.fixedincome

import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import kotlinx.coroutines.flow.Flow

interface FixedIncomeLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<FixedIncomePosition>>
    fun getOpenByAccount(accountId: String): Flow<List<FixedIncomePosition>>
    fun getById(id: String): Flow<FixedIncomePosition?>
    fun getNearMaturity(accountId: String, thresholdDate: Long): Flow<List<FixedIncomePosition>>
    suspend fun insert(position: FixedIncomePosition): Result<Unit>
    suspend fun update(position: FixedIncomePosition): Result<Unit>
    suspend fun close(id: String, closedAt: Long, closeType: String): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
}