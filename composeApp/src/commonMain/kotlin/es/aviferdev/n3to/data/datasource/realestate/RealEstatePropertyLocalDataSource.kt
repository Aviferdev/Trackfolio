package es.aviferdev.n3to.data.datasource.realestate

import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalPeriod
import kotlinx.coroutines.flow.Flow

interface RealEstatePropertyLocalDataSource {
    // ── RealEstateProperty ────────────────────────────────────────────────────
    fun getByAccount(accountId: String): Flow<List<RealEstateProperty>>
    fun getActiveByAccount(accountId: String): Flow<List<RealEstateProperty>>
    fun getById(id: String): Flow<RealEstateProperty?>
    suspend fun insert(property: RealEstateProperty): Result<Unit>
    suspend fun updateEstimatedValue(id: String, value: Double): Result<Unit>
    suspend fun updateLinkedLoan(id: String, loanId: String): Result<Unit>
    suspend fun dismissMortgageReminder(id: String): Result<Unit>
    suspend fun archive(id: String): Result<Unit>
    suspend fun sell(id: String, saleDate: Long, saleValue: Double): Result<Unit>

    // ── RentalPeriod (fusionado) ──────────────────────────────────────────────
    fun getRentalPeriodsByProperty(propertyId: String): Flow<List<RentalPeriod>>
    fun getActiveRentalPeriodByProperty(propertyId: String): Flow<RentalPeriod?>
    suspend fun insertRentalPeriod(period: RentalPeriod): Result<Unit>
    suspend fun closeRentalPeriod(periodId: String, endDate: Long): Result<Unit>
}
