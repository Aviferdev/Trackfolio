package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.AssetEntity
import es.aviferdev.trackfolio.domain.model.Asset

fun AssetEntity.toDomain(): Asset = Asset(
    id            = id,
    accountId     = accountId,
    ticker        = ticker,
    name          = name,
    quantity      = quantity,
    purchasePrice = purchasePrice,
    purchaseDate  = purchaseDate,
    notes         = notes,
    createdAt     = createdAt
)

fun Asset.toEntity(): AssetEntity = AssetEntity(
    id            = id,
    accountId     = accountId,
    ticker        = ticker,
    name          = name,
    quantity      = quantity,
    purchasePrice = purchasePrice,
    purchaseDate  = purchaseDate,
    notes         = notes,
    createdAt     = createdAt
)
