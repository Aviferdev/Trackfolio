package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.RealEstateProperty
import kotlinx.coroutines.flow.Flow

interface RealEstatePropertyRepository {
    fun getPropertiesByAccount(accountId: String): Flow<List<RealEstateProperty>>
    fun getActiveByAccount(accountId: String): Flow<List<RealEstateProperty>>
    fun getPropertyById(id: String): Flow<RealEstateProperty?>
    suspend fun saveProperty(property: RealEstateProperty): Result<Unit>
    suspend fun updateEstimatedValue(id: String, value: Double): Result<Unit>
    suspend fun archiveProperty(id: String): Result<Unit>
    suspend fun linkLoan(propertyId: String, loanId: String): Result<Unit>
    suspend fun dismissMortgageReminder(propertyId: String): Result<Unit>
}
