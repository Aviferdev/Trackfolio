package es.aviferdev.trackfolio.domain.model

data class Debt(
    val id: String,
    val personName: String,
    val amount: Double,
    val direction: DebtDirection,
    val date: Long,
    val isPaid: Boolean,
    val notes: String?,
    val createdAt: Long
)

enum class DebtDirection { I_OWE, THEY_OWE }
