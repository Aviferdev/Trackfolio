package es.aviferdev.n3to.data.datasource.asset

import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.MonthlyInvestment
import kotlinx.coroutines.flow.Flow

interface AssetTransactionLocalDataSource {
    fun getByAsset(assetId: String): Flow<List<AssetTransaction>>
    fun getByAssetDesc(assetId: String): Flow<List<AssetTransaction>>
    fun getByAccount(accountId: String): Flow<List<AssetTransaction>>
    fun getById(id: String): Flow<AssetTransaction?>
    fun countByPlatform(platformId: String): Flow<Long>
    /** Inversión mensual (compras) para un año. */
    fun getMonthlyInvestmentsByYear(accountId: String, year: String): Flow<List<MonthlyInvestment>>

    suspend fun insert(tx: AssetTransaction): Result<Unit>
    suspend fun update(tx: AssetTransaction): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    fun getOldestDate(accountId: String): Flow<Long?>
}
