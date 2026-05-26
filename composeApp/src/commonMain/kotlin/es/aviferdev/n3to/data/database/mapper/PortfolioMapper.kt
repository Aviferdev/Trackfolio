package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.PortfolioEntity
import es.aviferdev.n3to.domain.model.Portfolio

fun PortfolioEntity.toDomain(): Portfolio = Portfolio(
    id = id,
    accountId = accountId,
    name = name,
    description = description,
    color = color,
    sortOrder = sortOrder.toInt(),
    createdAt = createdAt,
    archived = archived != 0L
)

fun Portfolio.toEntity(): PortfolioEntity = PortfolioEntity(
    id = id,
    accountId = accountId,
    name = name,
    description = description,
    color = color,
    sortOrder = sortOrder.toLong(),
    createdAt = createdAt,
    archived = if (archived) 1L else 0L
)
