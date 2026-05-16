package es.aviferdev.n3to.data.datasource.asset

import es.aviferdev.n3to.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<Asset>>
    fun getAllByAccountIncludingArchived(accountId: String): Flow<List<Asset>>
    fun getById(id: String): Flow<Asset?>
    suspend fun insert(asset: Asset): Result<Unit>
    suspend fun update(asset: Asset): Result<Unit>
    suspend fun updateCurrentPrice(id: String, price: Double, updatedAt: Long): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    fun getOutdatedByAccount(accountId: String, thresholdDate: Long): Flow<List<Asset>>
}
