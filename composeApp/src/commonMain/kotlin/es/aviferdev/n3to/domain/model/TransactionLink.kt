package es.aviferdev.n3to.domain.model

/**
 * Vínculo polimórfico entre una [Transaction] y otra entidad del dominio.
 * Reemplaza los campos linked* de [Transaction].
 *
 * @property id ID único del vínculo.
 * @property linkType Tipo de entidad vinculada.
 * @property linkedEntityId ID de la entidad vinculada (AssetTransaction, Loan, Property, Valuable, etc.).
 * @property assetId Solo para [DIVIDEND] y [BOND_DEPOSIT]: ID del [AssetEntity] asociado.
 */
data class TransactionLink(
    val id: String,
    val linkType: TransactionLinkType,
    val linkedEntityId: String,
    val assetId: String? = null
)
