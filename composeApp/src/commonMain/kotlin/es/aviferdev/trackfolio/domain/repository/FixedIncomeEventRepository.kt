package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import kotlinx.coroutines.flow.Flow

interface FixedIncomeEventRepository {
    fun getByPosition(positionId: String): Flow<List<FixedIncomeEvent>>
    fun getByAccount(accountId: String): Flow<List<FixedIncomeEvent>>
    fun totalCollectedByPosition(positionId: String): Flow<Double>
    suspend fun insert(event: FixedIncomeEvent): Result<Unit>
    suspend fun update(event: FixedIncomeEvent): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}