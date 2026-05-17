package es.aviferdev.n3to.domain.usecase.emergencyfund

import es.aviferdev.n3to.domain.model.EmergencyFund
import es.aviferdev.n3to.domain.repository.EmergencyFundRepository
import kotlinx.coroutines.flow.Flow

class GetEmergencyFundUseCase(
    private val repository: EmergencyFundRepository
) {
    operator fun invoke(accountId: String): Flow<EmergencyFund?> =
        repository.getEmergencyFund(accountId)
}
