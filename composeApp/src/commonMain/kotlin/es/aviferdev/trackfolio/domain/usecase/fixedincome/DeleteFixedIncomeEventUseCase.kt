package es.aviferdev.trackfolio.domain.usecase.fixedincome

import es.aviferdev.trackfolio.domain.repository.FixedIncomeEventRepository

class DeleteFixedIncomeEventUseCase(
    private val eventRepository: FixedIncomeEventRepository,
    private val deleteLinkedTransactionUseCase: DeleteLinkedTransactionUseCase
) {
    suspend operator fun invoke(eventId: String, linkedTransactionId: String?): Result<Unit> {
        linkedTransactionId?.let {
            val txDeleteResult = deleteLinkedTransactionUseCase(it)
            if (txDeleteResult.isFailure) return txDeleteResult
        }
        return eventRepository.delete(eventId)
    }
}

class DeleteLinkedTransactionUseCase(
    private val transactionRepository: es.aviferdev.trackfolio.domain.repository.TransactionRepository
) {
    suspend operator fun invoke(transactionId: String): Result<Unit> =
        transactionRepository.deleteTransaction(transactionId)
}