package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.model.DebtDirection
import kotlinx.coroutines.flow.Flow

interface DebtRepository {
    fun getActive(): Flow<List<Debt>>
    fun getAll(): Flow<List<Debt>>
    fun getTotalByDirection(direction: DebtDirection): Flow<Double>
    suspend fun save(debt: Debt): Result<Unit>
    suspend fun update(debt: Debt): Result<Unit>
    suspend fun markAsPaid(id: String): Result<Unit>
}
