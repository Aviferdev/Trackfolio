package es.aviferdev.n3to.data.datasource.inflation

import es.aviferdev.n3to.domain.model.InflationDataPoint

interface InflationRemoteDataSource {
    suspend fun fetchHistory(countryCode: String, years: Int): Result<List<InflationDataPoint>>
}
