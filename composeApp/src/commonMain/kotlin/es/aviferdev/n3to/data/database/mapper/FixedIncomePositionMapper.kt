package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.FixedIncomePositionEntity
import es.aviferdev.n3to.domain.model.*

fun FixedIncomePositionEntity.toDomain(): FixedIncomePosition = FixedIncomePosition(
    id = id,
    accountId = accountId,
    portfolioId = portfolioId,
    assetCategoryId = assetCategoryId,
    name = name,
    ticker = ticker,
    type = FixedIncomeType.valueOf(type),
    notes = notes,
    principal = principal,
    quantity = quantity,
    nominalPerUnit = nominalPerUnit,
    interestRate = interestRate,
    interestFrequency = InterestFrequency.valueOf(interestFrequency),
    startDate = startDate,
    maturityDate = maturityDate,
    platformId = platformId,
    issuerId = issuerId,
    regionId = regionId,
    sectorId = sectorId,
    autoRenew = autoRenew != 0L,
    archived = archived != 0L,
    closedAt = closedAt,
    closeType = closeType?.let { FixedIncomeCloseType.valueOf(it) },
    feeNote = feeNote,
    createdAt = createdAt
)

fun FixedIncomePosition.toEntity(): FixedIncomePositionEntity = FixedIncomePositionEntity(
    id = id,
    accountId = accountId,
    portfolioId = portfolioId,
    assetCategoryId = assetCategoryId,
    name = name,
    ticker = ticker,
    type = type.name,
    notes = notes,
    principal = principal,
    quantity = quantity,
    nominalPerUnit = nominalPerUnit,
    interestRate = interestRate,
    interestFrequency = interestFrequency.name,
    startDate = startDate,
    maturityDate = maturityDate,
    platformId = platformId,
    issuerId = issuerId,
    regionId = regionId,
    sectorId = sectorId,
    autoRenew = if (autoRenew) 1L else 0L,
    archived = if (archived) 1L else 0L,
    closedAt = closedAt,
    closeType = closeType?.name,
    feeNote = feeNote,
    createdAt = createdAt
)