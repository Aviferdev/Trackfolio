package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.FixedIncomePositionEntity
import es.aviferdev.trackfolio.domain.model.*

fun FixedIncomePositionEntity.toDomain(): FixedIncomePosition = FixedIncomePosition(
    id                = id,
    accountId         = accountId,
    assetCategoryId   = assetCategoryId,
    name              = name,
    ticker            = ticker,
    type              = FixedIncomeType.valueOf(type),
    notes             = notes,
    principal         = principal,
    quantity          = quantity,
    nominalPerUnit    = nominalPerUnit,
    interestRate      = interestRate,
    interestFrequency = InterestFrequency.valueOf(interestFrequency),
    startDate         = startDate,
    maturityDate      = maturityDate,
    platformId        = platformId,
    issuerId          = issuerId,
    autoRenew         = autoRenew != 0L,
    archived          = archived != 0L,
    closedAt          = closedAt,
    closeType         = closeType?.let { FixedIncomeCloseType.valueOf(it) },
    feeNote           = feeNote,
    createdAt         = createdAt
)

fun FixedIncomePosition.toEntity(): FixedIncomePositionEntity = FixedIncomePositionEntity(
    id                = id,
    accountId         = accountId,
    assetCategoryId   = assetCategoryId,
    name              = name,
    ticker            = ticker,
    type              = type.name,
    notes             = notes,
    principal         = principal,
    quantity          = quantity,
    nominalPerUnit    = nominalPerUnit,
    interestRate      = interestRate,
    interestFrequency = interestFrequency.name,
    startDate         = startDate,
    maturityDate      = maturityDate,
    platformId        = platformId,
    issuerId          = issuerId,
    autoRenew         = if (autoRenew) 1L else 0L,
    archived          = if (archived) 1L else 0L,
    closedAt          = closedAt,
    closeType         = closeType?.name,
    feeNote           = feeNote,
    createdAt         = createdAt
)