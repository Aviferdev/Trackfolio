package es.aviferdev.n3to.domain.usecase.emergencyfund

import es.aviferdev.n3to.domain.model.EmergencyFund
import es.aviferdev.n3to.domain.repository.EmergencyFundRepository

class SaveEmergencyFundUseCase(
    private val repository: EmergencyFundRepository
) {
    /** Guarda la configuración del fondo. */
    suspend operator fun invoke(fund: EmergencyFund) {
        repository.saveEmergencyFund(fund)
    }

    /** Desactiva el fondo para la cuenta (borrado lógico). */
    suspend operator fun invoke(accountId: String) {
        repository.deleteEmergencyFund(accountId)
    }
}
