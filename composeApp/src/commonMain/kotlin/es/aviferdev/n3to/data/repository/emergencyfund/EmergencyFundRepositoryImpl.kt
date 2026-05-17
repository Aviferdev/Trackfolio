package es.aviferdev.n3to.data.repository.emergencyfund

import es.aviferdev.n3to.data.datasource.emergencyfund.EmergencyFundLocalDataSource
import es.aviferdev.n3to.domain.model.EmergencyFund
import es.aviferdev.n3to.domain.repository.EmergencyFundRepository
import kotlinx.coroutines.flow.Flow

class EmergencyFundRepositoryImpl(
    private val dataSource: EmergencyFundLocalDataSource
) : EmergencyFundRepository {

    override fun getEmergencyFund(accountId: String): Flow<EmergencyFund?> =
        dataSource.getEmergencyFund(accountId)

    override suspend fun saveEmergencyFund(fund: EmergencyFund) =
        dataSource.saveEmergencyFund(fund)

    override suspend fun deleteEmergencyFund(accountId: String) =
        dataSource.deleteEmergencyFund(accountId)
}
