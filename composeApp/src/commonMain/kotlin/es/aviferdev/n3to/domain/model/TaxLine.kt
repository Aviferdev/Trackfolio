package es.aviferdev.n3to.domain.model

data class TaxLine(
    val name: String,
    val role: TaxRole,
    val percent: Double?,
    val amount: Double
)
