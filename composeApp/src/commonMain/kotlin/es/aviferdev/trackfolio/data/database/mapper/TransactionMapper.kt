package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.TransactionEntity
import es.aviferdev.trackfolio.domain.model.IncomeTaxType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id          = id,
    accountId   = accountId,
    amount      = amount,
    type        = TransactionType.valueOf(type),
    categoryId  = categoryId,
    date        = date,
    notes       = notes,
    createdAt   = createdAt,
    grossAmount = grossAmount,
    irpfPercent = irpfPercent,
    taxType     = IncomeTaxType.fromName(taxType)
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id          = id,
    accountId   = accountId,
    amount      = amount,
    type        = type.name,
    categoryId  = categoryId,
    date        = date,
    notes       = notes,
    createdAt   = createdAt,
    grossAmount = grossAmount,
    irpfPercent = irpfPercent,
    taxType     = taxType?.name
)
