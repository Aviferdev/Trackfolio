package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.DebtEntity
import es.aviferdev.trackfolio.domain.model.Debt
import es.aviferdev.trackfolio.domain.model.DebtDirection

fun DebtEntity.toDomain(): Debt = Debt(
    id         = id,
    accountId  = accountId,
    personName = personName,
    amount     = amount,
    direction  = DebtDirection.valueOf(direction),
    date       = date,
    isPaid     = isPaid != 0L,
    notes      = notes,
    createdAt  = createdAt
)

fun Debt.toEntity(): DebtEntity = DebtEntity(
    id         = id,
    accountId  = accountId,
    personName = personName,
    amount     = amount,
    direction  = direction.name,
    date       = date,
    isPaid     = if (isPaid) 1L else 0L,
    notes      = notes,
    createdAt  = createdAt
)
