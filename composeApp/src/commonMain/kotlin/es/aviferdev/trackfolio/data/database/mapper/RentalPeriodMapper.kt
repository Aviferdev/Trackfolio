package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.RentalPeriodEntity
import es.aviferdev.trackfolio.domain.model.RentalPeriod

fun RentalPeriodEntity.toDomain(): RentalPeriod = RentalPeriod(
    id          = id,
    propertyId  = propertyId,
    startDate   = startDate,
    endDate     = endDate,
    monthlyRent = monthlyRent,
    notes       = notes
)

fun RentalPeriod.toEntity(): RentalPeriodEntity = RentalPeriodEntity(
    id          = id,
    propertyId  = propertyId,
    startDate   = startDate,
    endDate     = endDate,
    monthlyRent = monthlyRent,
    notes       = notes
)
