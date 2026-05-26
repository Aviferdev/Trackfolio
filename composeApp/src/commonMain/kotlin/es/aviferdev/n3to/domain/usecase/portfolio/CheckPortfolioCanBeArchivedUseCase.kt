package es.aviferdev.n3to.domain.usecase.portfolio

import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.domain.repository.AssetRepository
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
import kotlinx.coroutines.flow.first

data class PortfolioOpenItems(
    val openAssets: List<Asset> = emptyList(),
    val openFixedIncome: List<FixedIncomePosition> = emptyList()
) {
    val isEmpty: Boolean get() = openAssets.isEmpty() && openFixedIncome.isEmpty()
}

class CheckPortfolioCanBeArchivedUseCase(
    private val assetRepository: AssetRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val fixedIncomeRepository: FixedIncomeRepository
) {
    suspend operator fun invoke(portfolioId: String): PortfolioOpenItems {
        val assets = assetRepository.getAssetsByPortfolio(portfolioId).first()
        val openAssets = assets.filter { asset ->
            val txs = assetTransactionRepository.getByAsset(asset.id).first()
            PortfolioCalculator.calculate(txs, asset.currentPrice).netQuantity > 0.0
        }
        val openFixedIncome = fixedIncomeRepository.getByPortfolio(portfolioId).first()
            .filter { it.isOpen }
        return PortfolioOpenItems(openAssets, openFixedIncome)
    }
}
