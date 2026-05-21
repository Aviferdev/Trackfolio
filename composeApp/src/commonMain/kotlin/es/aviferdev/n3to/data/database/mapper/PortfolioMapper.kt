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
    createdAt = createdAt
)

fun Portfolio.toEntity(): PortfolioEntity = PortfolioEntity(
    id = id,
    accountId = accountId,
    name = name,
    description = description,
    color = color,
    sortOrder = sortOrder.toLong(),
    createdAt = createdAt
)
