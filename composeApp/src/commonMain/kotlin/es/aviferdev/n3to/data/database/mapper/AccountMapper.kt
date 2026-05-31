package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.AccountEntity
import es.aviferdev.n3to.data.database.GetAllComputedBalances
import es.aviferdev.n3to.data.database.GetComputedBalance
import es.aviferdev.n3to.domain.model.Account

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    initialBalance = initialBalance,
    computedBalance = initialBalance,  // sin joins, el balance = saldo inicial
    createdAt = createdAt,
    currency = currency,
    archived = archived != 0L
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    initialBalance = initialBalance,
    createdAt = createdAt,
    currency = currency,
    archived = if (archived) 1L else 0L
)

fun GetComputedBalance.toDomain(): Account = Account(
    id = id,
    name = name,
    initialBalance = initialBalance,
    computedBalance = computedBalance,
    createdAt = createdAt,
    currency = currency,
    archived = archived != 0L
)

fun GetAllComputedBalances.toDomain(): Account = Account(
    id = id,
    name = name,
    initialBalance = initialBalance,
    computedBalance = computedBalance,
    createdAt = createdAt,
    currency = currency,
    archived = archived != 0L
)
