package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.EmergencyFund
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para la configuración del fondo de emergencia.
 */
interface EmergencyFundRepository {
    /** Obtiene la configuración del fondo para una cuenta. */
    fun getEmergencyFund(accountId: String): Flow<EmergencyFund?>

    /** Guarda o actualiza la configuración. */
    suspend fun saveEmergencyFund(fund: EmergencyFund)

    /** Elimina la configuración (desactiva el fondo). */
    suspend fun deleteEmergencyFund(accountId: String)
}
