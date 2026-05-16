package es.aviferdev.n3to.domain.model

data class FixedIncomeEvent(
    val id: String,
    val positionId: String,
    val type: FixedIncomeEventType,
    val grossAmount: Double,
    val irpfPercent: Double,
    val commissionAmount: Double,
    val netAmount: Double,
    val date: Long,
    val notes: String?,
    val createdAt: Long
)