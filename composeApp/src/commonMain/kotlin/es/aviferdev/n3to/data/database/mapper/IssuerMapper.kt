package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.BankEntity
import es.aviferdev.n3to.data.database.BondIssuerEntity
import es.aviferdev.n3to.data.database.DividendSourceEntity
import es.aviferdev.n3to.data.database.EmployerEntity
import es.aviferdev.n3to.data.database.ExemptSourceEntity
import es.aviferdev.n3to.data.database.PromotionPlatformEntity
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType

fun EmployerEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.EMPLOYER,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)

fun BankEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.BANK,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)

fun BondIssuerEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.BOND_ISSUER,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)

fun DividendSourceEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.DIVIDEND_SOURCE,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)

fun PromotionPlatformEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.PROMOTION_PLATFORM,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)

fun ExemptSourceEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.EXEMPT_SOURCE,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)
