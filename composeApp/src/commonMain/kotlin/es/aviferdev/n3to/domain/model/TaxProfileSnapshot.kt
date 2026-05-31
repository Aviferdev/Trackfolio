package es.aviferdev.n3to.domain.model

import kotlinx.datetime.LocalDate

data class TaxProfileSnapshot(
    val id: String,
    val profile: TaxProfile,
    val effectiveFrom: LocalDate,
    val createdAt: Long
)
