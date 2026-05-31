package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.model.IncomeTaxDetails
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionLink
import es.aviferdev.n3to.domain.model.TransactionLinkType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository

class RegisterCouponUseCase(
    private val eventRepository: FixedIncomeEventRepository,
    private val recordIncomeTransactionUseCase: RecordIncomeTransactionUseCase
) {
    suspend operator fun invoke(event: FixedIncomeEvent, accountId: String, assetId: String): Result<Unit> {
        val eventResult = eventRepository.insert(event)
        if (eventResult.isFailure) return eventResult

        val ledgerResult = recordIncomeTransactionUseCase(
            accountId = accountId,
            amount = event.netAmount,
            date = event.date,
            notes = event.notes ?: "Cupón / Interés",
            linkedEventId = "fi_${event.id}",
            assetId = assetId
        )

        return ledgerResult
    }
}

class RecordIncomeTransactionUseCase(
    private val transactionRepository: es.aviferdev.n3to.domain.repository.TransactionRepository
) {
    suspend operator fun invoke(
        accountId: String,
        amount: Double,
        date: Long,
        notes: String?,
        linkedEventId: String?,
        assetId: String?
    ): Result<Unit> = runCatching {
        val link = linkedEventId?.let {
            TransactionLink(
                id = "link_$it",
                linkType = TransactionLinkType.BOND_DEPOSIT,
                linkedEntityId = it,
                assetId = assetId
            )
        }
        val txId = "tx_${nowMillis()}"
        val tx = Transaction(
            id = txId,
            accountId = accountId,
            amount = amount,
            type = TransactionType.INCOME,
            date = date,
            categoryId = null,
            taxDetails = IncomeTaxDetails(
                transactionId = txId,
                incomeType = IncomeType.BOND_DEPOSIT
            ),
            notes = notes,
            links = link?.let { listOf(it) } ?: emptyList(),
            createdAt = nowMillis()
        )
        transactionRepository.saveTransaction(tx)
    }
}
