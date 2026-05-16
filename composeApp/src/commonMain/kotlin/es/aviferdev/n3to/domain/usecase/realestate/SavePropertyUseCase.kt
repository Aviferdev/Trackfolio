package es.aviferdev.n3to.domain.usecase.realestate

import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.domain.repository.RealEstatePropertyRepository

class SavePropertyUseCase(
    private val repository: RealEstatePropertyRepository
) {
    suspend operator fun invoke(property: RealEstateProperty): Result<Unit> {
        require(property.ownershipPercentage in 0.0..100.0) {
            "El porcentaje de propiedad debe estar entre 0 y 100"
        }
        require(property.name.isNotBlank()) { "El nombre no puede estar vacío" }
        require(property.address.isNotBlank()) { "La dirección no puede estar vacía" }
        require(property.purchaseValue > 0) { "El valor de compra debe ser mayor que 0" }
        require(property.currentEstimatedValue > 0) { "El valor estimado debe ser mayor que 0" }
        require(property.acquisitionDate > 0) { "La fecha de adquisición es obligatoria" }

        if (property.rentalStatus == RentalStatus.RENTED) {
            require(property.monthlyRent != null && property.monthlyRent > 0) {
                "La renta mensual es obligatoria para propiedades alquiladas"
            }
        }

        return repository.saveProperty(property)
    }
}
