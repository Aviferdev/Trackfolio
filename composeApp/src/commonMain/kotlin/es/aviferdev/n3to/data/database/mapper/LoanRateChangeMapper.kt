package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.LoanRateChangeEntity
import es.aviferdev.n3to.domain.model.LoanRateChange

fun LoanRateChangeEntity.toDomain(): LoanRateChange = LoanRateChange(
    id = id,
    loanId = loanId,
    newRate = newRate,
    previousRate = previousRate,
    effectiveDate = effectiveDate,
    createdAt = createdAt,
    archived = archived != 0L
)

fun LoanRateChange.toEntity(): LoanRateChangeEntity = LoanRateChangeEntity(
    id = id,
    loanId = loanId,
    newRate = newRate,
    previousRate = previousRate,
    effectiveDate = effectiveDate,
    createdAt = createdAt,
    archived = if (archived) 1L else 0L
)
