package es.aviferdev.n3to.data.repository.inflation

import es.aviferdev.n3to.data.datasource.inflation.InflationRemoteDataSource
import es.aviferdev.n3to.domain.model.InflationDataPoint
import es.aviferdev.n3to.domain.repository.InflationRepository
import es.aviferdev.n3to.platform.nowMillis

private const val CACHE_TTL_MS = 24L * 60 * 60 * 1000

class InflationRepositoryImpl(
    private val remote: InflationRemoteDataSource
) : InflationRepository {

    private val cache = mutableMapOf<String, Pair<Long, List<InflationDataPoint>>>()

    override suspend fun getHistory(
        countryCodes: List<String>,
        years: Int
    ): Map<String, List<InflationDataPoint>> {
        val result = mutableMapOf<String, List<InflationDataPoint>>()
        val now = nowMillis()
        for (code in countryCodes) {
            val cached = cache[code]
            if (cached != null && now - cached.first < CACHE_TTL_MS) {
                result[code] = cached.second
            } else {
                remote.fetchHistory(code, years).onSuccess { data ->
                    cache[code] = Pair(now, data)
                    result[code] = data
                }
            }
        }
        return result
    }
}
