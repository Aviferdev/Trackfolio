package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.FixedIncomeEventEntity
import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.model.FixedIncomeEventType

fun FixedIncomeEventEntity.toDomain(): FixedIncomeEvent = FixedIncomeEvent(
    id               = id,
    positionId       = positionId,
    type             = FixedIncomeEventType.valueOf(type),
    grossAmount      = grossAmount,
    irpfPercent      = irpfPercent,
    commissionAmount = commissionAmount,
    netAmount        = netAmount,
    date             = date,
    notes            = notes,
    createdAt        = createdAt
)

fun FixedIncomeEvent.toEntity(): FixedIncomeEventEntity = FixedIncomeEventEntity(
    id               = id,
    positionId       = positionId,
    type             = type.name,
    grossAmount      = grossAmount,
    irpfPercent      = irpfPercent,
    commissionAmount = commissionAmount,
    netAmount        = netAmount,
    date             = date,
    notes            = notes,
    createdAt        = createdAt
)