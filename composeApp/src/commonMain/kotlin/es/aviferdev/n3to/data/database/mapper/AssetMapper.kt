package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.AssetCategoryEntity
import es.aviferdev.n3to.data.database.AssetEntity
import es.aviferdev.n3to.data.database.SelectAssetsWithBrokenIsin
import es.aviferdev.n3to.data.database.SelectQuotableByAccount
import es.aviferdev.n3to.data.database.AssetRegionDistributionEntity
import es.aviferdev.n3to.data.database.AssetRegionEntity
import es.aviferdev.n3to.data.database.AssetSectorEntity
import es.aviferdev.n3to.data.database.AssetSectorRelationEntity
import es.aviferdev.n3to.data.database.AssetTagEntity
import es.aviferdev.n3to.data.database.AssetTransactionEntity
import es.aviferdev.n3to.data.database.PlatformEntity
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.PriceSource
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetRegionDistribution
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.AssetSectorRelation
import es.aviferdev.n3to.domain.model.AssetTag
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.Platform
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private fun safePriceSource(name: String): PriceSource =
    PriceSource.entries.firstOrNull { it.name == name } ?: PriceSource.MANUAL

fun AssetEntity.toDomain(): Asset = Asset(
    id = id,
    accountId = accountId,
    portfolioId = portfolioId,
    ticker = ticker,
    name = name,
    notes = notes,
    createdAt = createdAt,
    assetCategoryId = assetCategoryId,
    currentPrice = currentPrice,
    currentPriceUpdatedAt = currentPriceUpdatedAt,
    archived = archived != 0L,
    maturityDate = maturityDate,
    isin = isin,
    priceSource = safePriceSource(priceSource),
    isinValidatedAt = isinValidatedAt,
    isinValidationError = isinValidationError,
    fixedIncomePercent = fixedIncomePercent.toInt()
)

fun Asset.toEntity(): AssetEntity = AssetEntity(
    id = id,
    accountId = accountId,
    portfolioId = portfolioId,
    ticker = ticker,
    name = name,
    notes = notes,
    createdAt = createdAt,
    assetCategoryId = assetCategoryId,
    currentPrice = currentPrice,
    currentPriceUpdatedAt = currentPriceUpdatedAt,
    archived = if (archived) 1L else 0L,
    maturityDate = maturityDate,
    isin = isin,
    priceSource = priceSource.name,
    isinValidatedAt = isinValidatedAt,
    isinValidationError = isinValidationError,
    fixedIncomePercent = fixedIncomePercent.toLong()
)

fun AssetCategoryEntity.toDomain(): AssetCategory = AssetCategory(
    id = id,
    name = name,
    icon = icon,
    sortOrder = sortOrder.toInt(),
    archived = archived != 0L,
    createdAt = createdAt
)

fun AssetCategory.toEntity(): AssetCategoryEntity = AssetCategoryEntity(
    id = id,
    name = name,
    icon = icon,
    sortOrder = sortOrder.toLong(),
    archived = if (archived) 1L else 0L,
    createdAt = createdAt
)

fun AssetTagEntity.toDomain(): AssetTag = AssetTag(
    id = id,
    name = name,
    categoryId = categoryId,
    color = color,
    archived = archived != 0L,
    createdAt = createdAt
)

fun AssetTag.toEntity(): AssetTagEntity = AssetTagEntity(
    id = id,
    name = name,
    categoryId = categoryId,
    color = color,
    archived = if (archived) 1L else 0L,
    createdAt = createdAt
)

fun PlatformEntity.toDomain(): Platform = Platform(
    id = id,
    name = name,
    icon = icon,
    sortOrder = sortOrder.toInt(),
    archived = archived != 0L,
    createdAt = createdAt,
    notes = notes
)

fun Platform.toEntity(): PlatformEntity = PlatformEntity(
    id = id,
    name = name,
    icon = icon,
    sortOrder = sortOrder.toLong(),
    archived = if (archived) 1L else 0L,
    createdAt = createdAt,
    notes = notes
)

fun AssetTransactionEntity.toDomain(): AssetTransaction = AssetTransaction(
    id = id,
    assetId = assetId,
    type = AssetTransactionType.valueOf(type),
    quantity = quantity,
    pricePerUnit = pricePerUnit,
    date = date,
    platformId = platformId,
    feeNote = feeNote,
    notes = notes,
    createdAt = createdAt
)

fun AssetTransaction.toEntity(): AssetTransactionEntity {
    val local = Instant.fromEpochMilliseconds(date)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val year = local.year.toString()
    val month = local.monthNumber.toString().padStart(2, '0')
    return AssetTransactionEntity(
        id = id,
        assetId = assetId,
        type = type.name,
        quantity = quantity,
        pricePerUnit = pricePerUnit,
        date = date,
        year = year,
        month = month,
        platformId = platformId,
        feeNote = feeNote,
        notes = notes,
        createdAt = createdAt
    )
}

fun AssetSectorEntity.toDomain(): AssetSector = AssetSector(
    id = id,
    name = name,
    icon = icon,
    createdAt = createdAt
)

fun AssetSector.toEntity(): AssetSectorEntity = AssetSectorEntity(
    id = id,
    name = name,
    icon = icon,
    createdAt = createdAt
)

// ── Conversión desde tipos de query específicos ───────────────────────────

fun SelectQuotableByAccount.toDomain(): Asset = Asset(
    id = id,
    accountId = accountId,
    portfolioId = portfolioId,
    ticker = ticker,
    name = name,
    notes = notes,
    createdAt = createdAt,
    assetCategoryId = assetCategoryId,
    currentPrice = currentPrice,
    currentPriceUpdatedAt = currentPriceUpdatedAt,
    archived = archived != 0L,
    maturityDate = maturityDate,
    isin = isin,
    priceSource = safePriceSource(priceSource),
    isinValidatedAt = isinValidatedAt,
    isinValidationError = isinValidationError,
    fixedIncomePercent = fixedIncomePercent.toInt()
)

fun SelectAssetsWithBrokenIsin.toDomain(): Asset = Asset(
    id = id,
    accountId = accountId,
    portfolioId = portfolioId,
    ticker = ticker,
    name = name,
    notes = notes,
    createdAt = createdAt,
    assetCategoryId = assetCategoryId,
    currentPrice = currentPrice,
    currentPriceUpdatedAt = currentPriceUpdatedAt,
    archived = archived != 0L,
    maturityDate = maturityDate,
    isin = isin,
    priceSource = safePriceSource(priceSource),
    isinValidatedAt = isinValidatedAt,
    isinValidationError = isinValidationError,
    fixedIncomePercent = fixedIncomePercent.toInt()
)

fun AssetSectorRelationEntity.toDomain(): AssetSectorRelation = AssetSectorRelation(
    assetId = assetId,
    sectorId = sectorId
)

fun AssetSectorRelation.toEntity(): AssetSectorRelationEntity = AssetSectorRelationEntity(
    assetId = assetId,
    sectorId = sectorId
)

fun AssetRegionEntity.toDomain(): AssetRegion = AssetRegion(
    id = id,
    name = name,
    createdAt = createdAt
)

fun AssetRegion.toEntity(): AssetRegionEntity = AssetRegionEntity(
    id = id,
    name = name,
    createdAt = createdAt
)

fun AssetRegionDistributionEntity.toDomain(): AssetRegionDistribution = AssetRegionDistribution(
    assetId = assetId,
    regionId = regionId,
    percent = percent.toInt()
)

fun AssetRegionDistribution.toEntity(): AssetRegionDistributionEntity =
    AssetRegionDistributionEntity(
        assetId = assetId,
        regionId = regionId,
        percent = percent.toLong()
    )
