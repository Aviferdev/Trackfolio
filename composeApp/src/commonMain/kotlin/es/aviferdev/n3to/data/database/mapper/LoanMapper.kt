package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.LoanEntity
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.LoanCloseType
import es.aviferdev.n3to.domain.model.LoanType

fun LoanEntity.toDomain(): Loan = Loan(
    id = id,
    accountId = accountId,
    name = name,
    type = LoanType.fromName(type),
    totalAmount = totalAmount,
    outstandingPrincipal = outstandingPrincipal,
    currentInterestRate = currentInterestRate,
    monthlyPayment = monthlyPayment,
    totalInstallments = totalInstallments.toInt(),
    paidInstallments = paidInstallments.toInt(),
    startDate = startDate,
    endDate = endDate,
    lenderName = lenderName,
    notes = notes,
    archived = archived != 0L,
    closedAt = closedAt,
    closeType = closeType?.let { LoanCloseType.valueOf(it) },
    createdAt = createdAt
)

fun Loan.toEntity(): LoanEntity = LoanEntity(
    id = id,
    accountId = accountId,
    name = name,
    type = type.name,
    totalAmount = totalAmount,
    outstandingPrincipal = outstandingPrincipal,
    currentInterestRate = currentInterestRate,
    monthlyPayment = monthlyPayment,
    totalInstallments = totalInstallments.toLong(),
    paidInstallments = paidInstallments.toLong(),
    startDate = startDate,
    endDate = endDate,
    lenderName = lenderName,
    notes = notes,
    archived = if (archived) 1L else 0L,
    closedAt = closedAt,
    closeType = closeType?.name,
    createdAt = createdAt
)
