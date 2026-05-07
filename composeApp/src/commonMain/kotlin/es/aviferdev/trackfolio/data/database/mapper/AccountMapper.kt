package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.GetAllComputedBalances
import es.aviferdev.trackfolio.data.database.GetComputedBalance
import es.aviferdev.trackfolio.domain.model.Account

fun GetComputedBalance.toDomain(): Account = Account(
    id              = id,
    name            = name,
    currency        = currency,
    initialBalance  = initialBalance,
    computedBalance = computedBalance,
    createdAt       = createdAt
)

fun GetAllComputedBalances.toDomain(): Account = Account(
    id              = id,
    name            = name,
    currency        = currency,
    initialBalance  = initialBalance,
    computedBalance = computedBalance,
    createdAt       = createdAt
)
