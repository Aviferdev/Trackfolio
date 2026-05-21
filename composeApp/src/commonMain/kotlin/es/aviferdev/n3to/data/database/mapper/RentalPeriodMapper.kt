package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.RentalPeriodEntity
import es.aviferdev.n3to.domain.model.RentalPeriod

fun RentalPeriodEntity.toDomain(): RentalPeriod = RentalPeriod(
    id = id,
    propertyId = propertyId,
    startDate = startDate,
    endDate = endDate,
    monthlyRent = monthlyRent,
    notes = notes
)

fun RentalPeriod.toEntity(): RentalPeriodEntity = RentalPeriodEntity(
    id = id,
    propertyId = propertyId,
    startDate = startDate,
    endDate = endDate,
    monthlyRent = monthlyRent,
    notes = notes
)
