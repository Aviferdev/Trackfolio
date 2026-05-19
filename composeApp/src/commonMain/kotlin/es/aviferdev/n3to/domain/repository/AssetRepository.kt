package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.PriceSource
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getAssetsByAccount(accountId: String): Flow<List<Asset>>
    fun getAssetsByPortfolio(portfolioId: String): Flow<List<Asset>>
    fun getAssetsWithoutPortfolio(accountId: String): Flow<List<Asset>>
    fun getAllByAccountIncludingArchived(accountId: String): Flow<List<Asset>>
    fun getAssetById(id: String): Flow<Asset?>
    suspend fun saveAsset(asset: Asset): Result<Unit>
    suspend fun updateAsset(asset: Asset): Result<Unit>
    suspend fun updateCurrentPrice(id: String, price: Double, updatedAt: Long): Result<Unit>
    suspend fun archiveAsset(id: String): Result<Unit>
    suspend fun unarchiveAsset(id: String): Result<Unit>
    suspend fun deleteAsset(id: String): Result<Unit>
    fun getOutdatedAssets(accountId: String, thresholdDate: Long): Flow<List<Asset>>

    // ── Nuevos métodos para auto-precio e ISIN ──
    suspend fun updateIsin(id: String, isin: String?, validatedAt: Long?): Result<Unit>
    suspend fun updatePriceSource(id: String, priceSource: PriceSource): Result<Unit>
    suspend fun markIsinValidationError(id: String, error: String?): Result<Unit>
    fun getQuotableAssets(accountId: String): Flow<List<Asset>>
    fun getAssetsWithBrokenIsin(accountId: String): Flow<List<Asset>>
}
