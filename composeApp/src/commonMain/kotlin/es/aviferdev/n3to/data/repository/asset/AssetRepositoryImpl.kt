package es.aviferdev.n3to.data.repository.asset

import es.aviferdev.n3to.data.datasource.asset.AssetLocalDataSource
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.PriceSource
import es.aviferdev.n3to.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow

class AssetRepositoryImpl(
    private val dataSource: AssetLocalDataSource
) : AssetRepository {

    override fun getAssetsByAccount(accountId: String): Flow<List<Asset>> =
        dataSource.getByAccount(accountId)

    override fun getAssetsByPortfolio(portfolioId: String): Flow<List<Asset>> =
        dataSource.getByPortfolio(portfolioId)

    override fun getAssetsWithoutPortfolio(accountId: String): Flow<List<Asset>> =
        dataSource.getByAccountWithoutPortfolio(accountId)

    override fun getAllByAccountIncludingArchived(accountId: String): Flow<List<Asset>> =
        dataSource.getAllByAccountIncludingArchived(accountId)

    override fun getAssetById(id: String): Flow<Asset?> =
        dataSource.getById(id)

    override suspend fun saveAsset(asset: Asset): Result<Unit> =
        dataSource.insert(asset)

    override suspend fun updateAsset(asset: Asset): Result<Unit> =
        dataSource.update(asset)

    override suspend fun updateCurrentPrice(id: String, price: Double, updatedAt: Long): Result<Unit> =
        dataSource.updateCurrentPrice(id, price, updatedAt)

    override suspend fun archiveAsset(id: String): Result<Unit> =
        dataSource.archive(id)

    override suspend fun unarchiveAsset(id: String): Result<Unit> =
        dataSource.unarchive(id)

    override suspend fun deleteAsset(id: String): Result<Unit> =
        dataSource.delete(id)

    override fun getOutdatedAssets(accountId: String, thresholdDate: Long): Flow<List<Asset>> =
        dataSource.getOutdatedByAccount(accountId, thresholdDate)

    // ── Nuevos métodos para auto-precio e ISIN ──

    override suspend fun updateIsin(id: String, isin: String?, validatedAt: Long?): Result<Unit> =
        dataSource.updateIsin(id, isin, validatedAt)

    override suspend fun updatePriceSource(id: String, priceSource: PriceSource): Result<Unit> =
        dataSource.updatePriceSource(id, priceSource)

    override suspend fun markIsinValidationError(id: String, error: String?): Result<Unit> =
        dataSource.markIsinValidationError(id, error)

    override fun getQuotableAssets(accountId: String): Flow<List<Asset>> =
        dataSource.getQuotableByAccount(accountId)

    override fun getAssetsWithBrokenIsin(accountId: String): Flow<List<Asset>> =
        dataSource.getAssetsWithBrokenIsin(accountId)
}
