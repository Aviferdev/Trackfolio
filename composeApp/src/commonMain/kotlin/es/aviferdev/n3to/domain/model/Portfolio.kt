package es.aviferdev.n3to.domain.model

data class Portfolio(
    val id: String,
    val accountId: String,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long
)
