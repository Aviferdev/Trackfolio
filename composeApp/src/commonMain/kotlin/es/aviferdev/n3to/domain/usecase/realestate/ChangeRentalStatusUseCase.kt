package es.aviferdev.n3to.domain.usecase.realestate

import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.RentalPeriod
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.domain.model.ValidationError
import es.aviferdev.n3to.domain.repository.RentalPeriodRepository
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository
import kotlinx.coroutines.flow.firstOrNull

class ChangeRentalStatusUseCase(
    private val propertyRepository: RealEstatePropertyRepository,
    private val rentalPeriodRepository: RentalPeriodRepository
) {
    /**
     * Cambia el estado de alquiler de una propiedad.
     * - Si había período activo, lo cierra con effectiveDate.
     * - Si el nuevo estado es RENTED, abre un nuevo RentalPeriod.
     * - Actualiza rentalStatus y monthlyRent en la propiedad.
     */
    suspend operator fun invoke(
        propertyId: String,
        newStatus: RentalStatus,
        effectiveDate: Long,
        monthlyRent: Double? = null
    ): Result<Unit> {
        // 1. Cerrar período activo si existe
        val activePeriod =
            rentalPeriodRepository.getActivePeriodByProperty(propertyId).firstOrNull()
        activePeriod?.let {
            rentalPeriodRepository.closeRentalPeriod(it.id, effectiveDate)
        }

        // 2. Si nuevo estado es RENTED, crear nuevo período
        if (newStatus == RentalStatus.RENTED) {
            if (monthlyRent == null || monthlyRent <= 0)
                return Result.failure(ValidationError.RentalIncomeRequired)
            rentalPeriodRepository.openRentalPeriod(
                RentalPeriod(
                    id = uuid4().toString(),
                    propertyId = propertyId,
                    startDate = effectiveDate,
                    endDate = null,
                    monthlyRent = monthlyRent,
                    notes = null
                )
            )
        }

        // 3. Actualizar la propiedad
        val property = propertyRepository.getPropertyById(propertyId).firstOrNull()
            ?: return Result.failure(Exception("Propiedad no encontrada"))

        val updated = property.copy(
            rentalStatus = newStatus,
            monthlyRent = if (newStatus == RentalStatus.RENTED) monthlyRent else null
        )

        return propertyRepository.saveProperty(updated)
    }
}
