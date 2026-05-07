package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.AssetCategoryEntity
import es.aviferdev.trackfolio.data.database.AssetEntity
import es.aviferdev.trackfolio.data.database.AssetTagEntity
import es.aviferdev.trackfolio.data.database.AssetTransactionEntity
import es.aviferdev.trackfolio.data.database.PlatformEntity
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.AssetTag
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.Platform

fun AssetEntity.toDomain(): Asset = Asset(
    id                    = id,
    accountId             = accountId,
    ticker                = ticker,
    name                  = name,
    notes                 = notes,
    createdAt             = createdAt,
    assetCategoryId       = assetCategoryId,
    currentPrice          = currentPrice,
    currentPriceUpdatedAt = currentPriceUpdatedAt,
    archived              = archived != 0L
)

fun Asset.toEntity(): AssetEntity = AssetEntity(
    id                    = id,
    accountId             = accountId,
    ticker                = ticker,
    name                  = name,
    notes                 = notes,
    createdAt             = createdAt,
    assetCategoryId       = assetCategoryId,
    currentPrice          = currentPrice,
    currentPriceUpdatedAt = currentPriceUpdatedAt,
    archived              = if (archived) 1L else 0L
)

fun AssetCategoryEntity.toDomain(): AssetCategory = AssetCategory(
    id        = id,
    name      = name,
    icon      = icon,
    sortOrder = sortOrder.toInt(),
    archived  = archived != 0L,
    createdAt = createdAt
)

fun AssetCategory.toEntity(): AssetCategoryEntity = AssetCategoryEntity(
    id        = id,
    name      = name,
    icon      = icon,
    sortOrder = sortOrder.toLong(),
    archived  = if (archived) 1L else 0L,
    createdAt = createdAt
)

fun AssetTagEntity.toDomain(): AssetTag = AssetTag(
    id         = id,
    name       = name,
    categoryId = categoryId,
    color      = color,
    archived   = archived != 0L,
    createdAt  = createdAt
)

fun AssetTag.toEntity(): AssetTagEntity = AssetTagEntity(
    id         = id,
    name       = name,
    categoryId = categoryId,
    color      = color,
    archived   = if (archived) 1L else 0L,
    createdAt  = createdAt
)

fun PlatformEntity.toDomain(): Platform = Platform(
    id        = id,
    name      = name,
    icon      = icon,
    sortOrder = sortOrder.toInt(),
    archived  = archived != 0L,
    createdAt = createdAt
)

fun Platform.toEntity(): PlatformEntity = PlatformEntity(
    id        = id,
    name      = name,
    icon      = icon,
    sortOrder = sortOrder.toLong(),
    archived  = if (archived) 1L else 0L,
    createdAt = createdAt
)

fun AssetTransactionEntity.toDomain(): AssetTransaction = AssetTransaction(
    id           = id,
    assetId      = assetId,
    type         = AssetTransactionType.valueOf(type),
    quantity     = quantity,
    pricePerUnit = pricePerUnit,
    date         = date,
    platformId   = platformId,
    feeNote      = feeNote,
    notes        = notes,
    createdAt    = createdAt
)

fun AssetTransaction.toEntity(): AssetTransactionEntity = AssetTransactionEntity(
    id           = id,
    assetId      = assetId,
    type         = type.name,
    quantity     = quantity,
    pricePerUnit = pricePerUnit,
    date         = date,
    platformId   = platformId,
    feeNote      = feeNote,
    notes        = notes,
    createdAt    = createdAt
)
