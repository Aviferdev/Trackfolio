package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.AssetPriceHistoryEntity
import es.aviferdev.trackfolio.domain.model.AssetPriceHistory

fun AssetPriceHistoryEntity.toDomain(): AssetPriceHistory = AssetPriceHistory(
    id         = id,
    assetId    = assetId,
    price      = price,
    recordedAt = recordedAt
)

fun AssetPriceHistory.toEntity(): AssetPriceHistoryEntity = AssetPriceHistoryEntity(
    id         = id,
    assetId    = assetId,
    price      = price,
    recordedAt = recordedAt
)
