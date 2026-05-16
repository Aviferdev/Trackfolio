package es.aviferdev.trackfolio.data.datasource.realestate

import es.aviferdev.trackfolio.domain.model.RealEstateProperty
import kotlinx.coroutines.flow.Flow

interface RealEstatePropertyLocalDataSource {
    fun getByAccount(accountId: String): Flow<List<RealEstateProperty>>
    fun getActiveByAccount(accountId: String): Flow<List<RealEstateProperty>>
    fun getById(id: String): Flow<RealEstateProperty?>
    suspend fun insert(property: RealEstateProperty): Result<Unit>
    suspend fun updateEstimatedValue(id: String, value: Double): Result<Unit>
    suspend fun updateLinkedLoan(id: String, loanId: String): Result<Unit>
    suspend fun dismissMortgageReminder(id: String): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
}
