package es.aviferdev.n3to.data.database.mapper

import es.aviferdev.n3to.data.database.TransactionLinkEntity
import es.aviferdev.n3to.domain.model.TransactionLink
import es.aviferdev.n3to.domain.model.TransactionLinkType

fun TransactionLinkEntity.toDomain(): TransactionLink = TransactionLink(
    id = id,
    linkType = TransactionLinkType.valueOf(linkType),
    linkedEntityId = linkedEntityId,
    assetId = assetId
)

fun TransactionLink.toEntity(transactionId: String): TransactionLinkEntity = TransactionLinkEntity(
    id = id,
    transactionId = transactionId,
    linkType = linkType.name,
    linkedEntityId = linkedEntityId,
    assetId = assetId
)
