package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<Asset>>
    fun getById(id: String): Flow<Asset?>
    fun getTotalInvestedByAccount(accountId: String): Flow<Double>
    suspend fun insert(asset: Asset): Result<Unit>
    suspend fun update(asset: Asset): Result<Unit>
    suspend fun updateCurrentPrice(id: String, price: Double, updatedAt: Long): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
