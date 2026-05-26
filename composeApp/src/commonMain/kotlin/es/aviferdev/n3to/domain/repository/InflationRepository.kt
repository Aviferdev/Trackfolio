package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.InflationDataPoint

interface InflationRepository {
    suspend fun getHistory(
        countryCodes: List<String>,
        years: Int = 20
    ): Map<String, List<InflationDataPoint>>
}
