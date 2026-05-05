package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.AssetCategoryEntity
import es.aviferdev.trackfolio.data.database.AssetEntity
import es.aviferdev.trackfolio.data.database.AssetTagEntity
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.AssetTag

// ─── Asset ────────────────────────────────────────────────────────────────────

fun AssetEntity.toDomain(): Asset = Asset(
    id              = id,
    accountId       = accountId,
    ticker          = ticker,
    name            = name,
    quantity        = quantity,
    purchasePrice   = purchasePrice,
    purchaseDate    = purchaseDate,
    notes           = notes,
    createdAt       = createdAt,
    assetCategoryId = assetCategoryId
)

fun Asset.toEntity(): AssetEntity = AssetEntity(
    id              = id,
    accountId       = accountId,
    ticker          = ticker,
    name            = name,
    quantity        = quantity,
    purchasePrice   = purchasePrice,
    purchaseDate    = purchaseDate,
    notes           = notes,
    createdAt       = createdAt,
    assetCategoryId = assetCategoryId
)

// ─── AssetCategory ────────────────────────────────────────────────────────────

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

// ─── AssetTag ─────────────────────────────────────────────────────────────────

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
