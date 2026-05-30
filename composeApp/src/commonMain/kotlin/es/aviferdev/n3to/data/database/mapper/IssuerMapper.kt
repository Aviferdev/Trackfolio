package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.IssuerEntity
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType

fun IssuerEntity.toDomain(): Issuer = Issuer(
    id = id,
    accountId = accountId,
    name = name,
    type = IssuerType.valueOf(type),
    icon = icon,
    archived = archived != 0L,
    createdAt = createdAt
)

fun Issuer.toEntity(): IssuerEntity = IssuerEntity(
    id = id,
    accountId = accountId,
    name = name,
    icon = icon,
    type = type.name,
    archived = if (archived) 1L else 0L,
    createdAt = createdAt
)
