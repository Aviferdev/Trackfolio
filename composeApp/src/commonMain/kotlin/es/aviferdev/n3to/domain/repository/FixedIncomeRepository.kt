package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.FixedIncomePosition
import kotlinx.coroutines.flow.Flow

interface FixedIncomeRepository {
    fun getByAccount(accountId: String): Flow<List<FixedIncomePosition>>
    fun getOpenByAccount(accountId: String): Flow<List<FixedIncomePosition>>
    fun getById(id: String): Flow<FixedIncomePosition?>
    fun getNearMaturity(accountId: String, thresholdDate: Long): Flow<List<FixedIncomePosition>>
    fun getByAccountAndCategory(accountId: String, categoryId: String): Flow<List<FixedIncomePosition>>
    suspend fun insert(position: FixedIncomePosition): Result<Unit>
    suspend fun update(position: FixedIncomePosition): Result<Unit>
    suspend fun close(id: String, closedAt: Long, closeType: String): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
}