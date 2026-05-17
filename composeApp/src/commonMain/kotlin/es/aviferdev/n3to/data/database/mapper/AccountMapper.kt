package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.GetAllComputedBalances
import es.aviferdev.n3to.data.database.GetComputedBalance
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.AccountType

fun GetComputedBalance.toDomain(): Account = Account(
    id              = id,
    name            = name,
    initialBalance  = initialBalance,
    computedBalance = computedBalance,
    createdAt       = createdAt,
    accountType     = AccountType.fromName(accountType),
    currency        = currency
)

fun GetAllComputedBalances.toDomain(): Account = Account(
    id              = id,
    name            = name,
    initialBalance  = initialBalance,
    computedBalance = computedBalance,
    createdAt       = createdAt,
    accountType     = AccountType.fromName(accountType),
    currency        = currency
)
