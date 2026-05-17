package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.RealEstatePropertyEntity
import es.aviferdev.n3to.domain.model.PropertyType
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus

fun RealEstatePropertyEntity.toDomain(): RealEstateProperty = RealEstateProperty(
    id                        = id,
    accountId                 = accountId,
    name                      = name,
    address                   = address,
    propertyType              = PropertyType.fromName(propertyType),
    purchaseValue             = purchaseValue,
    currentEstimatedValue     = currentEstimatedValue,
    acquisitionDate           = acquisitionDate,
    ownershipPercentage       = ownershipPercentage,
    linkedLoanId              = linkedLoanId,
    rentalStatus              = RentalStatus.fromName(rentalStatus),
    monthlyRent               = monthlyRent,
    mortgageReminderDismissed = mortgageReminderDismissed != 0L,
    archived                  = archived != 0L,
    saleDate                  = saleDate,
    saleValue                 = saleValue
)

fun RealEstateProperty.toEntity(): RealEstatePropertyEntity = RealEstatePropertyEntity(
    id                        = id,
    accountId                 = accountId,
    name                      = name,
    address                   = address,
    propertyType              = propertyType.name,
    purchaseValue             = purchaseValue,
    currentEstimatedValue     = currentEstimatedValue,
    acquisitionDate           = acquisitionDate,
    ownershipPercentage       = ownershipPercentage,
    linkedLoanId              = linkedLoanId,
    rentalStatus              = rentalStatus.name,
    monthlyRent               = monthlyRent,
    mortgageReminderDismissed = if (mortgageReminderDismissed) 1L else 0L,
    archived                  = if (archived) 1L else 0L,
    saleDate                  = saleDate,
    saleValue                 = saleValue
)
