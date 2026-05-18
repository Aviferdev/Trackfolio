package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.ValuableEntity
import es.aviferdev.n3to.domain.model.Valuable

fun ValuableEntity.toDomain(): Valuable = Valuable(
    id             = id,
    accountId      = accountId,
    name           = name,
    description    = description,
    purchasePrice  = purchasePrice,
    purchaseDate   = purchaseDate,
    estimatedValue = estimatedValue,
    salePrice      = salePrice,
    saleDate       = saleDate,
    linkedLoanId   = linkedLoanId,
    notes          = notes,
    createdAt      = createdAt
)

fun Valuable.toEntity(): ValuableEntity = ValuableEntity(
    id             = id,
    accountId      = accountId,
    name           = name,
    description    = description,
    purchasePrice  = purchasePrice,
    purchaseDate   = purchaseDate,
    estimatedValue = estimatedValue,
    salePrice      = salePrice,
    saleDate       = saleDate,
    linkedLoanId   = linkedLoanId,
    notes          = notes,
    createdAt      = createdAt
)
