package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.AssetPriceHistoryEntity
import es.aviferdev.n3to.domain.model.AssetPriceHistory

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
