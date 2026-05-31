package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.TransactionEntity
import es.aviferdev.n3to.domain.model.IncomeTaxDetails
import es.aviferdev.n3to.domain.model.TaxLine
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionLink
import es.aviferdev.n3to.domain.model.TransactionType

fun TransactionEntity.toDomain(
    taxLines: List<TaxLine> = emptyList(),
    links: List<TransactionLink> = emptyList(),
    taxDetails: IncomeTaxDetails? = null
): Transaction = Transaction(
    id = id,
    accountId = accountId,
    amount = amount,
    type = TransactionType.valueOf(type),
    categoryId = categoryId,
    date = date,
    notes = notes,
    createdAt = createdAt,
    excludeFromFiscal = excludeFromFiscal != 0L,
    taxDetails = taxDetails,
    taxLines = taxLines,
    originalCurrency = originalCurrency,
    originalAmount = originalAmount,
    exchangeRate = exchangeRate,
    links = links
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    accountId = accountId,
    amount = amount,
    type = type.name,
    categoryId = categoryId,
    date = date,
    notes = notes,
    createdAt = createdAt,
    excludeFromFiscal = if (excludeFromFiscal) 1L else 0L,
    originalCurrency = originalCurrency,
    originalAmount = originalAmount,
    exchangeRate = exchangeRate
)
