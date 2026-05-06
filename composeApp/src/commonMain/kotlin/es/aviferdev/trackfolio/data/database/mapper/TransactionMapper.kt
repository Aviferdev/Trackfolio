package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.TransactionEntity
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id                   = id,
    accountId            = accountId,
    amount               = amount,
    type                 = TransactionType.valueOf(type),
    categoryId           = categoryId,
    date                 = date,
    notes                = notes,
    createdAt            = createdAt,
    incomeType           = IncomeType.fromName(incomeType),
    grossAmount          = grossAmount,
    irpfPercent          = irpfPercent,
    socialSecurityAmount = socialSecurityAmount,
    commissionAmount     = commissionAmount,
    issuerId             = issuerId,
    issuerName           = issuerName
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id                   = id,
    accountId            = accountId,
    amount               = amount,
    type                 = type.name,
    categoryId           = categoryId,
    date                 = date,
    notes                = notes,
    createdAt            = createdAt,
    incomeType           = incomeType?.name,
    grossAmount          = grossAmount,
    irpfPercent          = irpfPercent,
    socialSecurityAmount = socialSecurityAmount,
    commissionAmount     = commissionAmount,
    issuerId             = issuerId,
    issuerName           = issuerName
)
