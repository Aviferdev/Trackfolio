package es.aviferdev.n3to.domain.model

data class InflationDataPoint(
    val countryCode: String,
    val year: Int,
    val rate: Double
)
