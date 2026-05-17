package es.aviferdev.n3to.domain.model

data class TaxLineTemplate(
    val name: String,
    val role: TaxRole,
    val defaultPercent: Double?  // null = el usuario debe introducirlo manualmente
)
