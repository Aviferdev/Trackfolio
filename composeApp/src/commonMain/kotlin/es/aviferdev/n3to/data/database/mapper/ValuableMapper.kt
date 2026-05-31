package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.ValuableEntity
import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.model.ValuableCloseType

fun ValuableEntity.toDomain(): Valuable = Valuable(
    id = id,
    accountId = accountId,
    name = name,
    description = description,
    purchasePrice = purchasePrice,
    purchaseDate = purchaseDate,
    estimatedValue = estimatedValue,
    salePrice = salePrice,
    saleDate = saleDate,
    closeType = closeType?.let { ValuableCloseType.valueOf(it) },
    linkedLoanId = linkedLoanId,
    notes = notes,
    archived = archived != 0L,
    createdAt = createdAt
)

fun Valuable.toEntity(): ValuableEntity = ValuableEntity(
    id = id,
    accountId = accountId,
    name = name,
    description = description,
    purchasePrice = purchasePrice,
    purchaseDate = purchaseDate,
    estimatedValue = estimatedValue,
    salePrice = salePrice,
    saleDate = saleDate,
    closeType = closeType?.name,
    linkedLoanId = linkedLoanId,
    notes = notes,
    archived = if (archived) 1L else 0L,
    createdAt = createdAt
)
