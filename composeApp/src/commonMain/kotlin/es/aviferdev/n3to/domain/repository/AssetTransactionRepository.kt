package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.MonthlyInvestment
import es.aviferdev.n3to.domain.model.MonthlyNetInvestment
import kotlinx.coroutines.flow.Flow

interface AssetTransactionRepository {
    fun getByAsset(assetId: String): Flow<List<AssetTransaction>>
    fun getByAssetDesc(assetId: String): Flow<List<AssetTransaction>>
    fun getByAccount(accountId: String): Flow<List<AssetTransaction>>
    fun getById(id: String): Flow<AssetTransaction?>
    fun countByPlatform(platformId: String): Flow<Long>
    /** Inversión mensual (compras) para un año. */
    fun getMonthlyInvestmentsByYear(accountId: String, year: String): Flow<List<MonthlyInvestment>>

    /** Inversión neta mensual (BUY - SELL) para un año. */
    fun getMonthlyNetInvestmentsByYear(
        accountId: String,
        year: String
    ): Flow<List<MonthlyNetInvestment>>

    /** Inversión neta para un mes concreto. */
    fun getMonthlyNetInvestmentByMonth(
        accountId: String,
        yearMonth: String
    ): Flow<MonthlyNetInvestment?>

    suspend fun save(tx: AssetTransaction): Result<Unit>
    suspend fun update(tx: AssetTransaction): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    fun getOldestDate(accountId: String): Flow<Long?>
}
