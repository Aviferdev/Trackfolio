package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.DebtEntity
import es.aviferdev.n3to.domain.model.Debt
import es.aviferdev.n3to.domain.model.DebtDirection

fun DebtEntity.toDomain(): Debt = Debt(
    id = id,
    accountId = accountId,
    personName = personName,
    amount = amount,
    direction = DebtDirection.valueOf(direction),
    date = date,
    isPaid = isPaid != 0L,
    notes = notes,
    archived = archived != 0L,
    createdAt = createdAt
)

fun Debt.toEntity(): DebtEntity = DebtEntity(
    id = id,
    accountId = accountId,
    personName = personName,
    amount = amount,
    direction = direction.name,
    date = date,
    isPaid = if (isPaid) 1L else 0L,
    notes = notes,
    archived = if (archived) 1L else 0L,
    createdAt = createdAt
)
