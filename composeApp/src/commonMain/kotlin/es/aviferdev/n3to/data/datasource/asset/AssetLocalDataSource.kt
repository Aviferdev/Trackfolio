package es.aviferdev.n3to.data.datasource.asset

import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.PriceSource
import kotlinx.coroutines.flow.Flow

interface AssetLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<Asset>>
    fun getByPortfolio(portfolioId: String): Flow<List<Asset>>
    fun getByAccountWithoutPortfolio(accountId: String): Flow<List<Asset>>
    fun getAllByAccountIncludingArchived(accountId: String): Flow<List<Asset>>
    fun getById(id: String): Flow<Asset?>
    suspend fun insert(asset: Asset): Result<Unit>
    suspend fun update(asset: Asset): Result<Unit>
    suspend fun updateCurrentPrice(id: String, price: Double, updatedAt: Long): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun unarchive(id: String): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
    fun getOutdatedByAccount(accountId: String, thresholdDate: Long): Flow<List<Asset>>

    // ── Nuevos métodos para auto-precio e ISIN ──
    suspend fun updateIsin(id: String, isin: String?, validatedAt: Long?): Result<Unit>
    suspend fun updatePriceSource(id: String, priceSource: PriceSource): Result<Unit>
    suspend fun markIsinValidationError(id: String, error: String?): Result<Unit>
    fun getQuotableByAccount(accountId: String): Flow<List<Asset>>
    fun getAssetsWithBrokenIsin(accountId: String): Flow<List<Asset>>
}
