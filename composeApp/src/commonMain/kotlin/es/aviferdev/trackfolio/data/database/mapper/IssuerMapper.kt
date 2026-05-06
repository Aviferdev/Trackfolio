package es.aviferdev.trackfolio.data.database.mapper

import es.aviferdev.trackfolio.data.database.BankEntity
import es.aviferdev.trackfolio.data.database.BondIssuerEntity
import es.aviferdev.trackfolio.data.database.DividendSourceEntity
import es.aviferdev.trackfolio.data.database.EmployerEntity
import es.aviferdev.trackfolio.data.database.PromotionPlatformEntity
import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.IssuerType

// ── Employer ──────────────────────────────────────────────────────────────────

fun EmployerEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.EMPLOYER,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)

// ── Bank ──────────────────────────────────────────────────────────────────────

fun BankEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.BANK,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)

// ── BondIssuer ────────────────────────────────────────────────────────────────

fun BondIssuerEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.BOND_ISSUER,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)

// ── DividendSource ────────────────────────────────────────────────────────────

fun DividendSourceEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.DIVIDEND_SOURCE,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)

// ── PromotionPlatform ─────────────────────────────────────────────────────────

fun PromotionPlatformEntity.toDomain(): Issuer = Issuer(
    id        = id,
    accountId = accountId,
    name      = name,
    type      = IssuerType.PROMOTION_PLATFORM,
    icon      = icon,
    archived  = archived != 0L,
    createdAt = createdAt
)
