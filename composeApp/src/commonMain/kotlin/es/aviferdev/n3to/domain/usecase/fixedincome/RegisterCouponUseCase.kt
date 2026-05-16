package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import kotlinx.datetime.Clock

class RegisterCouponUseCase(
    private val eventRepository: FixedIncomeEventRepository,
    private val recordIncomeTransactionUseCase: RecordIncomeTransactionUseCase
) {
    suspend operator fun invoke(event: FixedIncomeEvent, accountId: String): Result<Unit> {
        val eventResult = eventRepository.insert(event)
        if (eventResult.isFailure) return eventResult

        val ledgerResult = recordIncomeTransactionUseCase(
            accountId    = accountId,
            amount       = event.netAmount,
            date         = event.date,
            notes        = event.notes ?: "Cupón / Interés",
            linkedEventId = "fi_${event.id}"
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
        linkedEventId: String?
    ): Result<Unit> = runCatching {
        val tx = Transaction(
            id                  = "tx_${Clock.System.now().toEpochMilliseconds()}",
            accountId           = accountId,
            amount              = amount,
            type                = TransactionType.INCOME,
            date                = date,
            categoryId          = null,
            incomeType          = IncomeType.BOND_DEPOSIT,
            grossAmount         = null,
            irpfPercent         = null,
            commissionAmount    = null,
            notes               = notes,
            linkedAssetTransactionId = linkedEventId,
            createdAt           = Clock.System.now().toEpochMilliseconds()
        )
        transactionRepository.saveTransaction(tx)
    }
}