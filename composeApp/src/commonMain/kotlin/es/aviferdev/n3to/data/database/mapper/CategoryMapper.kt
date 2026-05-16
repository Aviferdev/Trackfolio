package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.CategoryEntity
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.TransactionType

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    type = TransactionType.valueOf(type),
    isDefault = isDefault != 0L
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    type = type.name,
    isDefault = if (isDefault) 1L else 0L,
    archived = 0L
)
