package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.domain.model.TransactionLinkType
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository

class DeleteFixedIncomeEventUseCase(
    private val eventRepository: FixedIncomeEventRepository,
    private val deleteLinkedTransactionUseCase: DeleteLinkedTransactionUseCase
) {
    suspend operator fun invoke(eventId: String, linkedEntityId: String?): Result<Unit> {
        linkedEntityId?.let {
            val txDeleteResult = deleteLinkedTransactionUseCase(it)
            if (txDeleteResult.isFailure) return txDeleteResult
        }
        return eventRepository.delete(eventId)
    }
}

class DeleteLinkedTransactionUseCase(
    private val transactionRepository: es.aviferdev.n3to.domain.repository.TransactionRepository
) {
    suspend operator fun invoke(linkedEntityId: String): Result<Unit> =
        transactionRepository.deleteByLinkTypeAndEntityId(
            TransactionLinkType.BOND_DEPOSIT,
            linkedEntityId
        )
}
