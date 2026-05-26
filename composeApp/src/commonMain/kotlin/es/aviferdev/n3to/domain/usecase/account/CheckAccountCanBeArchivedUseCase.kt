package es.aviferdev.n3to.domain.usecase.account

import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.domain.repository.AssetRepository
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
import es.aviferdev.n3to.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.first

data class AccountOpenItems(
    val openAssetsCount: Int = 0,
    val openFixedIncomeCount: Int = 0
) {
    val isEmpty: Boolean get() = openAssetsCount == 0 && openFixedIncomeCount == 0
}

class CheckAccountCanBeArchivedUseCase(
    private val portfolioRepository: PortfolioRepository,
    private val assetRepository: AssetRepository,
    private val assetTransactionRepository: AssetTransactionRepository,
    private val fixedIncomeRepository: FixedIncomeRepository
) {
    suspend operator fun invoke(accountId: String): AccountOpenItems {
        val portfolios = portfolioRepository.getByAccount(accountId).first()
        var openAssetsCount = 0
        for (portfolio in portfolios) {
            val assets = assetRepository.getAssetsByPortfolio(portfolio.id).first()
            for (asset in assets) {
                val txs = assetTransactionRepository.getByAsset(asset.id).first()
                if (PortfolioCalculator.calculate(txs, asset.currentPrice).netQuantity > 0.0) {
                    openAssetsCount++
                }
            }
        }
        val openFixedIncomeCount = fixedIncomeRepository.getByAccount(accountId).first()
            .count { it.isOpen }
        return AccountOpenItems(openAssetsCount, openFixedIncomeCount)
    }
}
