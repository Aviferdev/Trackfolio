package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.CategoryEntity
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.TransactionType

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    accountId = accountId,
    name = name,
    type = TransactionType.valueOf(type),
    isDefault = isDefault != 0L,
    archived = archived != 0L,
    createdAt = createdAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    accountId = accountId,
    name = name,
    type = type.name,
    isDefault = if (isDefault) 1L else 0L,
    archived = if (archived) 1L else 0L,
    createdAt = createdAt
)
