package es.aviferdev.n3to.domain.model

data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val isDefault: Boolean
)
