package es.aviferdev.n3to.domain.model

data class LoanRateChange(
    val id: String,
    val loanId: String,
    val newRate: Double,
    val previousRate: Double,
    val effectiveDate: Long,
    val createdAt: Long
)
