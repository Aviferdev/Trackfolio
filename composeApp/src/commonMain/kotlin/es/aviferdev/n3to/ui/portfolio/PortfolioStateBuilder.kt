package es.aviferdev.n3to.ui.portfolio

import androidx.compose.ui.graphics.Color
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetComposition
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.model.FixedIncomeSummary
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.portfolio.CompoundEffectCalculator
import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.domain.portfolio.PositionInput
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.portfolio.home.AssetRow
import es.aviferdev.n3to.ui.portfolio.home.CategoryGroup
import es.aviferdev.n3to.ui.portfolio.home.CategorySlice
import es.aviferdev.n3to.ui.portfolio.home.DistributionView
import es.aviferdev.n3to.ui.portfolio.home.PortfolioUiState
import es.aviferdev.n3to.ui.theme.CategoryPalette
import es.aviferdev.n3to.ui.theme.PositiveGreen
import es.aviferdev.n3to.ui.theme.UncategorizedColor
import es.aviferdev.n3to.ui.theme.WarnAmber


data class PortfolioStateInput(
    val portfolioId: String?,
    val assets: List<Asset>,
    val categories: List<AssetCategory>,
    val account: Account?,
    val transactions: List<AssetTransaction>,
    val platforms: List<Platform>,
    val platformsByAsset: Map<String, List<Platform>>,
    val fiSummary: FixedIncomeSummary?,
    val nearMaturityPositions: List<FixedIncomePosition>,
    val accountId: String? = null,
    val compositions: List<AssetComposition> = emptyList(),
    val sectorRelations: List<AssetSectorRelation> = emptyList(),
    val regionDistributions: List<AssetRegionDistribution> = emptyList(),
    val bondIssuers: List<Issuer> = emptyList(),
    val bankIssuers: List<Issuer> = emptyList(),
    val dividendsByAsset: Map<String, List<Transaction>> = emptyMap(),
    val allSectors: List<AssetSector> = emptyList(),
    val allRegions: List<AssetRegion> = emptyList(),
    val selectedDistributionView: DistributionView = DistributionView.CATEGORY
)

class PortfolioStateBuilder {

    fun build(
        input: PortfolioStateInput,
        uncategorizedLabel: String = "No catalogados",
        variableIncomeLabel: String = "Renta variable"
    ): PortfolioUiState = with(input) {
        val filteredAssets = if (portfolioId == null) assets
        else assets.filter { it.portfolioId == portfolioId }

        val txByAsset: Map<String, List<AssetTransaction>> = transactions.groupBy { it.assetId }

        val allFiRows = fiSummary?.positions ?: emptyList()
        val fiRows = if (portfolioId == null) allFiRows
        else allFiRows.filter { it.position.portfolioId == portfolioId }

        val closedFiRows = if (portfolioId == null) (fiSummary?.closedPositions ?: emptyList())
        else (fiSummary?.closedPositions
            ?: emptyList()).filter { it.position.portfolioId == portfolioId }

        val fiByCategory = fiRows.groupBy { it.position.assetCategoryId }
        val categoryById = categories.associateBy { it.id }

        val openRows = mutableListOf<AssetRow>()
        val closedRows = mutableListOf<AssetRow>()
        for (asset in filteredAssets) {
            val txs = txByAsset[asset.id].orEmpty()
            val pos = PortfolioCalculator.calculate(txs, asset.currentPrice)
            val hasSales = txs.any { it.type == AssetTransactionType.SELL }
            when {
                pos.netQuantity > 0.0 -> openRows.add(AssetRow(asset, pos))
                hasSales -> closedRows.add(AssetRow(asset, pos))
            }
        }

        val byId = categories.associateBy { it.id }
        val grouped: Map<String?, List<AssetRow>> = openRows.groupBy { it.asset.assetCategoryId }

        val groups: List<CategoryGroup> = grouped.map { (categoryId, groupRows) ->
            val cat = categoryId?.let { byId[it] }
            val invested = groupRows.sumOf { it.position.totalInvestedRemaining }
            val current = groupRows.sumOf { it.position.currentValue }
            val realized = groupRows.sumOf { it.position.realizedPnL }
            val unrealized = groupRows.sumOf { it.position.unrealizedPnL }
            val total = realized + unrealized

            val fiRowsForCat = fiByCategory[categoryId].orEmpty()
            val fiInvested = fiRowsForCat.sumOf { it.position.principal }
            val fiCurrent = fiRowsForCat.sumOf { it.currentValue }
            val fiProfit = fiRowsForCat.sumOf { it.totalProfit }

            CategoryGroup(
                category = cat,
                rows = groupRows.sortedByDescending { it.position.currentValue },
                fixedIncomeRows = fiRowsForCat,
                totalInvested = invested + fiInvested,
                totalCurrentValue = current + fiCurrent,
                totalUnrealizedPnL = unrealized,
                totalRealizedPnL = realized,
                totalPnL = total + fiProfit,
                totalPnLPercent = if (invested + fiInvested > 0.0) ((total + fiProfit) / (invested + fiInvested)) * 100.0 else 0.0
            )
        }.sortedWith(
            compareBy(
                { if (it.category == null) 1 else 0 },
                { it.sortKey },
                { it.displayName }
            )
        )

        val stockCategoryIds = grouped.keys
        val fiOnlyCategoryIds = fiByCategory.keys - stockCategoryIds
        val fiOnlyGroups = fiOnlyCategoryIds.mapNotNull { categoryId ->
            val fiRowsForCat = fiByCategory[categoryId].orEmpty()
            if (fiRowsForCat.isEmpty()) return@mapNotNull null
            val cat = categoryId?.let { categoryById[it] }
            val fiInvested = fiRowsForCat.sumOf { it.position.principal }
            val fiCurrent = fiRowsForCat.sumOf { it.currentValue }
            val fiProfit = fiRowsForCat.sumOf { it.totalProfit }
            CategoryGroup(
                category = cat,
                rows = emptyList(),
                fixedIncomeRows = fiRowsForCat,
                totalInvested = fiInvested,
                totalCurrentValue = fiCurrent,
                totalUnrealizedPnL = 0.0,
                totalRealizedPnL = 0.0,
                totalPnL = fiProfit,
                totalPnLPercent = if (fiInvested > 0.0) (fiProfit / fiInvested) * 100.0 else 0.0
            )
        }

        val allGroups = (groups + fiOnlyGroups).sortedWith(
            compareBy(
                { if (it.category == null) 1 else 0 },
                { it.sortKey },
                { it.displayName }
            )
        )
        val regionGroups = buildRegionGroups(openRows, fiSummary?.positions.orEmpty(), uncategorizedLabel)

        val sectorGroups = buildSectorGroups(openRows, fiSummary?.positions.orEmpty(), uncategorizedLabel)

        val totalInvested = allGroups.sumOf { it.totalInvested }
        val totalCurrentValue = allGroups.sumOf { it.totalCurrentValue }
        val totalRealizedPnL = allGroups.sumOf { it.totalRealizedPnL } +
                closedRows.sumOf { it.position.realizedPnL }
        val totalUnrealizedPnL = allGroups.sumOf { it.totalUnrealizedPnL }
        val totalPnL = totalRealizedPnL + totalUnrealizedPnL

        val compoundEffect = buildCompoundEffect(openRows, txByAsset, dividendsByAsset)

        val fiTotalPrincipal = fiSummary?.totalPrincipal ?: 0.0
        val fiTotalCurrentValue = fiSummary?.totalCurrentValue ?: 0.0
        val fiTotalNetProfit = fiSummary?.totalNetProfit ?: 0.0

        val combinedInvested = totalInvested
        val combinedCurrentValue = totalCurrentValue
        val combinedPnL = totalPnL + fiTotalNetProfit
        val combinedPnLPercent =
            if (combinedInvested > 0.0) (combinedPnL / combinedInvested) * 100.0 else 0.0

        val distribution: List<CategorySlice> = if (combinedCurrentValue <= 0.0) emptyList()
        else allGroups
            .filter { it.totalCurrentValue > 0.0 }
            .mapIndexed { idx, g ->
                CategorySlice(
                    categoryId = g.category?.id,
                    name = g.displayName,
                    icon = g.displayIcon,
                    value = g.totalCurrentValue,
                    percent = (g.totalCurrentValue / combinedCurrentValue) * 100.0,
                    color = colorForGroup(g, idx)
                )
            }
            .sortedByDescending { it.percent }

        val compositionByAsset = compositions.associateBy { it.assetId }
        val compositionSlices = buildCompositionDistribution(
            allGroups, compositionByAsset, combinedCurrentValue,
            fiRows.sumOf { it.currentValue }, variableIncomeLabel
        )

        val assetCurrentValues = openRows.associate { it.asset.id to it.position.currentValue }
        val regionById = allRegions.associateBy { it.id }
        val regionValues = mutableMapOf<String, Double>()
        val catalogedAssetIds = mutableSetOf<String>()

        for (dist in regionDistributions) {
            val assetValue = assetCurrentValues[dist.assetId] ?: continue
            val weight = dist.percent / 100.0
            regionValues[dist.regionId] =
                (regionValues[dist.regionId] ?: 0.0) + (assetValue * weight)
            catalogedAssetIds.add(dist.assetId)
        }
        for ((assetId, value) in assetCurrentValues) {
            if (assetId !in catalogedAssetIds) {
                regionValues["__uncatalogued__"] = (regionValues["__uncatalogued__"] ?: 0.0) + value
            }
        }
        for (fiRow in fiRows) {
            val regionKey = fiRow.position.region
            if (regionKey != null) {
                regionValues[regionKey] = (regionValues[regionKey] ?: 0.0) + fiRow.currentValue
            } else {
                regionValues["__uncatalogued__"] = (regionValues["__uncatalogued__"] ?: 0.0) + fiRow.currentValue
            }
        }

        val regionSlices: List<CategorySlice> =
            if (combinedCurrentValue > 0.0 && regionValues.isNotEmpty()) {
                regionValues.entries.mapIndexed { idx, (regionId, value) ->
                    if (regionId == "__uncatalogued__") {
                        CategorySlice(
                            null,
                            uncategorizedLabel,
                            "❔",
                            value,
                            (value / combinedCurrentValue) * 100.0,
                            UncategorizedColor
                        )
                    } else {
                        val region = regionById[regionId]
                        CategorySlice(
                            regionId,
                            region?.name ?: regionId,
                            "🌍",
                            value,
                            (value / combinedCurrentValue) * 100.0,
                            CategoryPalette[idx % CategoryPalette.size]
                        )
                    }
                }.sortedByDescending { it.percent }
            } else emptyList()

        val sectorById = allSectors.associateBy { it.id }
        val sectorValues = mutableMapOf<String, Double>()
        val sectorsByAsset = sectorRelations.groupBy { it.assetId }
        val catalogedAssetIdsForSector = mutableSetOf<String>()

        for ((assetId, relations) in sectorsByAsset) {
            val assetValue = assetCurrentValues[assetId] ?: continue
            val valuePerSector = assetValue / relations.size
            for (rel in relations) {
                sectorValues[rel.sectorId] = (sectorValues[rel.sectorId] ?: 0.0) + valuePerSector
            }
            catalogedAssetIdsForSector.add(assetId)
        }
        for ((assetId, value) in assetCurrentValues) {
            if (assetId !in catalogedAssetIdsForSector) {
                sectorValues["__uncatalogued__"] = (sectorValues["__uncatalogued__"] ?: 0.0) + value
            }
        }
        for (fiRow in fiRows) {
            val sectorKey = fiRow.position.sector
            if (sectorKey != null) {
                sectorValues[sectorKey] = (sectorValues[sectorKey] ?: 0.0) + fiRow.currentValue
            } else {
                sectorValues["__uncatalogued__"] = (sectorValues["__uncatalogued__"] ?: 0.0) + fiRow.currentValue
            }
        }

        val sectorSlices: List<CategorySlice> =
            if (combinedCurrentValue > 0.0 && sectorValues.isNotEmpty()) {
                sectorValues.entries.mapIndexed { idx, (sectorId, value) ->
                    if (sectorId == "__uncatalogued__") {
                        CategorySlice(
                            null,
                            uncategorizedLabel,
                            "❔",
                            value,
                            (value / combinedCurrentValue) * 100.0,
                            UncategorizedColor
                        )
                    } else {
                        val sector = sectorById[sectorId]
                        CategorySlice(
                            sectorId,
                            sector?.name ?: sectorId,
                            sector?.icon ?: "📊",
                            value,
                            (value / combinedCurrentValue) * 100.0,
                            CategoryPalette[idx % CategoryPalette.size]
                        )
                    }
                }.sortedByDescending { it.percent }
            } else emptyList()

        return PortfolioUiState(
            groups = allGroups,
            regionGroups = regionGroups,
            sectorGroups = sectorGroups,
            closedPositions = closedRows.sortedByDescending { it.position.realizedPnL },
            closedFixedIncomePositions = closedFiRows,
            distribution = distribution,
            compositionDistribution = compositionSlices,
            regionDistribution = regionSlices,
            sectorDistribution = sectorSlices,
            selectedDistributionView = selectedDistributionView,
            totalInvested = totalInvested,
            totalCurrentValue = totalCurrentValue,
            totalRealizedPnL = totalRealizedPnL,
            totalUnrealizedPnL = totalUnrealizedPnL,
            totalPnL = totalPnL,
            totalPnLPercent = if (totalInvested > 0.0) (totalPnL / totalInvested) * 100.0 else 0.0,
            openPositionsCount = openRows.size,
            allAssets = assets,
            platforms = platforms,
            platformsByAsset = platformsByAsset,
            bondIssuers = bondIssuers,
            bankIssuers = bankIssuers,
            isLoading = false,
            fixedIncomeSummary = fiSummary,
            nearMaturityPositions = nearMaturityPositions,
            combinedInvested = combinedInvested,
            combinedCurrentValue = combinedCurrentValue,
            combinedPnL = combinedPnL,
            combinedPnLPercent = combinedPnLPercent,
            combinedRealizedPnL = totalRealizedPnL + (fiSummary?.totalCollectedInterest ?: 0.0),
            combinedUnrealizedPnL = totalUnrealizedPnL + (fiSummary?.totalAccruedInterest ?: 0.0),
            currentAccountId = accountId,
            allSectors = allSectors,
            allRegions = allRegions,
            compoundEffect = compoundEffect
        )
    }

    private fun buildCompoundEffect(
        openRows: List<AssetRow>,
        txByAsset: Map<String, List<AssetTransaction>>,
        dividendsByAsset: Map<String, List<Transaction>>
    ) = CompoundEffectCalculator.calculate(
        openRows.mapNotNull { row ->
            val txs = txByAsset[row.asset.id].orEmpty()
            if (txs.isEmpty()) return@mapNotNull null
            PositionInput(
                asset = row.asset,
                transactions = txs,
                currentPrice = row.asset.currentPrice,
                dividends = dividendsByAsset[row.asset.id].orEmpty()
            )
        },
        nowMillis()
    )

    private fun buildRegionGroups(
        openRows: List<AssetRow>,
        fiPositions: List<FixedIncomeRow>,
        uncategorizedLabel: String
    ): List<CategoryGroup> {
        val fiByRegion = fiPositions.groupBy { it.position.region }
        val assetsWithoutRegion = openRows  // assets don't have per-asset region metadata yet

        val allRegionKeys = mutableSetOf<String?>()
        allRegionKeys.addAll(fiByRegion.keys)
        if (assetsWithoutRegion.isNotEmpty()) allRegionKeys.add(uncategorizedLabel)

        return allRegionKeys.map { region ->
            val fiRows = fiByRegion[region].orEmpty()
            val assetRows = if (region == null) assetsWithoutRegion else emptyList()

            val invested = assetRows.sumOf { it.position.totalInvestedRemaining }
            val current = assetRows.sumOf { it.position.currentValue }
            val realized = assetRows.sumOf { it.position.realizedPnL }
            val unrealized = assetRows.sumOf { it.position.unrealizedPnL }
            val fiInvested = fiRows.sumOf { it.position.principal }
            val fiCurrent = fiRows.sumOf { it.currentValue }
            val fiProfit = fiRows.sumOf { it.totalProfit }

            CategoryGroup(
                category = null,
                customName = region ?: uncategorizedLabel,
                rows = assetRows.sortedByDescending { it.position.currentValue },
                fixedIncomeRows = fiRows,
                totalInvested = invested + fiInvested,
                totalCurrentValue = current + fiCurrent,
                totalUnrealizedPnL = unrealized,
                totalRealizedPnL = realized,
                totalPnL = (realized + unrealized) + fiProfit,
                totalPnLPercent = if (invested + fiInvested > 0.0)
                    ((realized + unrealized + fiProfit) / (invested + fiInvested)) * 100.0 else 0.0
            )
        }.sortedByDescending { it.totalCurrentValue }
    }

    private fun buildSectorGroups(
        openRows: List<AssetRow>,
        fiPositions: List<FixedIncomeRow>,
        uncategorizedLabel: String
    ): List<CategoryGroup> {
        val fiBySector = fiPositions.groupBy { it.position.sector }
        val assetsWithoutSector = openRows  // assets don't have per-asset sector metadata yet

        val allSectorKeys = mutableSetOf<String?>()
        allSectorKeys.addAll(fiBySector.keys)
        if (assetsWithoutSector.isNotEmpty()) allSectorKeys.add(uncategorizedLabel)

        return allSectorKeys.map { sector ->
            val fiRows = fiBySector[sector].orEmpty()
            val assetRows = if (sector == null) assetsWithoutSector else emptyList()

            val invested = assetRows.sumOf { it.position.totalInvestedRemaining }
            val current = assetRows.sumOf { it.position.currentValue }
            val realized = assetRows.sumOf { it.position.realizedPnL }
            val unrealized = assetRows.sumOf { it.position.unrealizedPnL }
            val fiInvested = fiRows.sumOf { it.position.principal }
            val fiCurrent = fiRows.sumOf { it.currentValue }
            val fiProfit = fiRows.sumOf { it.totalProfit }

            CategoryGroup(
                category = null,
                customName = sector ?: uncategorizedLabel,
                rows = assetRows.sortedByDescending { it.position.currentValue },
                fixedIncomeRows = fiRows,
                totalInvested = invested + fiInvested,
                totalCurrentValue = current + fiCurrent,
                totalUnrealizedPnL = unrealized,
                totalRealizedPnL = realized,
                totalPnL = (realized + unrealized) + fiProfit,
                totalPnLPercent = if (invested + fiInvested > 0.0)
                    ((realized + unrealized + fiProfit) / (invested + fiInvested)) * 100.0 else 0.0
            )
        }.sortedByDescending { it.totalCurrentValue }
    }

    private fun buildCompositionDistribution(
        groups: List<CategoryGroup>,
        compositionByAsset: Map<String, AssetComposition>,
        totalValue: Double,
        fiCurrentValue: Double,
        variableIncomeLabel: String
    ): List<CategorySlice> {
        if (totalValue <= 0.0) return emptyList()

        var rfValue = fiCurrentValue
        var rvValue = 0.0

        groups.forEach { group ->
            group.rows.forEach { assetRow ->
                val composition = compositionByAsset[assetRow.asset.id]
                val assetValue = assetRow.position.currentValue
                if (composition != null && composition.fixedIncomePercent > 0) {
                    val rfPart = assetValue * (composition.fixedIncomePercent / 100.0)
                    rfValue += rfPart
                    rvValue += assetValue - rfPart
                } else {
                    rvValue += assetValue
                }
            }
        }

        return buildList {
            if (rfValue > 0) add(
                CategorySlice(
                    "rf",
                    "Renta fija",
                    "🏦",
                    rfValue,
                    (rfValue / totalValue) * 100.0,
                    WarnAmber
                )
            )
            if (rvValue > 0) add(
                CategorySlice(
                    "rv",
                    variableIncomeLabel,
                    "📈",
                    rvValue,
                    (rvValue / totalValue) * 100.0,
                    PositiveGreen
                )
            )
        }.sortedByDescending { it.percent }
    }

    private fun colorForGroup(group: CategoryGroup, fallbackIndex: Int): Color {
        val cat = group.category ?: return UncategorizedColor
        val idx = if (cat.sortOrder >= 0) cat.sortOrder else fallbackIndex
        return CategoryPalette[idx % CategoryPalette.size]
    }
}
