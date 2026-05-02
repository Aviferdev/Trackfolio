package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.AccountEntity
import es.aviferdev.trackfolio.domain.model.Account
import es.aviferdev.trackfolio.domain.model.AccountType

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    type = AccountType.valueOf(type),
    currency = currency,
    balance = balance,
    createdAt = createdAt
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    type = type.name,
    currency = currency,
    balance = balance,
    createdAt = createdAt
)
