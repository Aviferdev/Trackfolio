package es.aviferdev.n3to.domain.usecase.inflation

import es.aviferdev.n3to.domain.model.InflationDataPoint
import es.aviferdev.n3to.domain.repository.InflationRepository

class GetInflationHistoryUseCase(
    private val repository: InflationRepository
) {
    suspend operator fun invoke(
        countryCodes: List<String>
    ): Map<String, List<InflationDataPoint>> =
        repository.getHistory(countryCodes)
}
