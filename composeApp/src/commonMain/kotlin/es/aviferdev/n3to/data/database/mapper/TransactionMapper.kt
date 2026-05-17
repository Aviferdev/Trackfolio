package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.TransactionEntity
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.TaxLine
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType

fun TransactionEntity.toDomain(taxLines: List<TaxLine> = emptyList()): Transaction = Transaction(
    id                         = id,
    accountId                  = accountId,
    amount                     = amount,
    type                       = TransactionType.valueOf(type),
    categoryId                 = categoryId,
    date                       = date,
    notes                      = notes,
    createdAt                  = createdAt,
    excludeFromFiscal          = excludeFromFiscal != 0L,
    incomeType                 = IncomeType.fromName(incomeType),
    grossAmount                = grossAmount,
    commissionAmount           = commissionAmount,
    issuerId                   = issuerId,
    issuerName                 = issuerName,
    taxLines                   = taxLines,
    originalCurrency           = originalCurrency,
    originalAmount             = originalAmount,
    exchangeRate               = exchangeRate,
    linkedAssetTransactionId   = linkedAssetTransactionId,
    linkedLoanId               = linkedLoanId,
    linkedPropertyId           = linkedPropertyId
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id                         = id,
    accountId                  = accountId,
    amount                     = amount,
    type                       = type.name,
    categoryId                 = categoryId,
    date                       = date,
    notes                      = notes,
    createdAt                  = createdAt,
    excludeFromFiscal          = if (excludeFromFiscal) 1L else 0L,
    incomeType                 = incomeType?.name,
    grossAmount                = grossAmount,
    commissionAmount           = commissionAmount,
    issuerId                   = issuerId,
    issuerName                 = issuerName,
    originalCurrency           = originalCurrency,
    originalAmount             = originalAmount,
    exchangeRate               = exchangeRate,
    linkedAssetTransactionId   = linkedAssetTransactionId,
    linkedLoanId               = linkedLoanId,
    linkedPropertyId           = linkedPropertyId
)
