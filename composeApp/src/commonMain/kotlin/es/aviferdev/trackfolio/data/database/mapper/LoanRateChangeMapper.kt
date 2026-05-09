package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.LoanRateChangeEntity
import es.aviferdev.trackfolio.domain.model.LoanRateChange

fun LoanRateChangeEntity.toDomain(): LoanRateChange = LoanRateChange(
    id            = id,
    loanId        = loanId,
    newRate       = newRate,
    previousRate  = previousRate,
    effectiveDate = effectiveDate,
    createdAt     = createdAt
)

fun LoanRateChange.toEntity(): LoanRateChangeEntity = LoanRateChangeEntity(
    id            = id,
    loanId        = loanId,
    newRate       = newRate,
    previousRate  = previousRate,
    effectiveDate = effectiveDate,
    createdAt     = createdAt
)
