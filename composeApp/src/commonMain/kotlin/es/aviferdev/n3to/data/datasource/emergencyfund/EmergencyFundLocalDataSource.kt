package es.aviferdev.n3to.data.datasource.emergencyfund

import es.aviferdev.n3to.domain.model.EmergencyFund
import kotlinx.coroutines.flow.Flow

/**
 * Fuente de datos local para el fondo de emergencia.
 * Almacena los datos en AppSettings (SharedPreferences / NSUserDefaults).
 */
interface EmergencyFundLocalDataSource {
    fun getEmergencyFund(accountId: String): Flow<EmergencyFund?>
    suspend fun saveEmergencyFund(fund: EmergencyFund)
    suspend fun deleteEmergencyFund(accountId: String)
}
