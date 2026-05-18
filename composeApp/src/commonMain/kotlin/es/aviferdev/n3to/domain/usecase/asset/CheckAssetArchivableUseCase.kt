package es.aviferdev.n3to.domain.usecase.asset

import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import kotlinx.coroutines.flow.first

class CheckAssetArchivableUseCase(
    private val assetTransactionRepository: AssetTransactionRepository
) {
    suspend operator fun invoke(assetId: String, currentPrice: Double?): Boolean {
        val txs = assetTransactionRepository.getByAsset(assetId).first()
        val position = PortfolioCalculator.calculate(txs, currentPrice)
        return position.netQuantity <= 0.0
    }
}
