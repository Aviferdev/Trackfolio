package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.IncomeTaxDetailsEntity
import es.aviferdev.n3to.domain.model.IncomeTaxDetails
import es.aviferdev.n3to.domain.model.IncomeType

fun IncomeTaxDetailsEntity.toDomain(issuerName: String? = null): IncomeTaxDetails = IncomeTaxDetails(
    transactionId = transactionId,
    incomeType = incomeType?.let { IncomeType.fromName(it) },
    grossAmount = grossAmount,
    commissionAmount = commissionAmount,
    issuerId = issuerId,
    issuerName = issuerName
)

fun IncomeTaxDetails.toEntity(): IncomeTaxDetailsEntity = IncomeTaxDetailsEntity(
    transactionId = transactionId,
    incomeType = incomeType?.name,
    grossAmount = grossAmount,
    commissionAmount = commissionAmount,
    issuerId = issuerId
)
