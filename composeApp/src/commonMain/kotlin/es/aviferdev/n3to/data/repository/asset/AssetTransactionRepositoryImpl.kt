package es.aviferdev.n3to.data.repository.asset

import es.aviferdev.n3to.data.datasource.asset.AssetTransactionLocalDataSource
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.MonthlyInvestment
import es.aviferdev.n3to.domain.model.MonthlyNetInvestment
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import kotlinx.coroutines.flow.Flow

class AssetTransactionRepositoryImpl(
    private val dataSource: AssetTransactionLocalDataSource
) : AssetTransactionRepository {

    override fun getByAsset(assetId: String): Flow<List<AssetTransaction>> =
        dataSource.getByAsset(assetId)

    override fun getByAssetDesc(assetId: String): Flow<List<AssetTransaction>> =
        dataSource.getByAssetDesc(assetId)

    override fun getByAccount(accountId: String): Flow<List<AssetTransaction>> =
        dataSource.getByAccount(accountId)

    override fun getById(id: String): Flow<AssetTransaction?> =
        dataSource.getById(id)

    override fun countByPlatform(platformId: String): Flow<Long> =
        dataSource.countByPlatform(platformId)

    override fun getMonthlyInvestmentsByYear(accountId: String, year: String): Flow<List<MonthlyInvestment>> =
        dataSource.getMonthlyInvestmentsByYear(accountId, year)

    override fun getMonthlyNetInvestmentsByYear(
        accountId: String,
        year: String
    ): Flow<List<MonthlyNetInvestment>> =
        dataSource.getMonthlyNetInvestmentsByYear(accountId, year)

    override fun getMonthlyNetInvestmentByMonth(
        accountId: String,
        yearMonth: String
    ): Flow<MonthlyNetInvestment?> =
        dataSource.getMonthlyNetInvestmentByMonth(accountId, yearMonth)

    override suspend fun save(tx: AssetTransaction): Result<Unit> = dataSource.insert(tx)
    override suspend fun update(tx: AssetTransaction): Result<Unit> = dataSource.update(tx)
    override suspend fun delete(id: String): Result<Unit> = dataSource.delete(id)

    override fun getOldestDate(accountId: String): Flow<Long?> =
        dataSource.getOldestDate(accountId)
}
