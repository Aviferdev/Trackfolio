package es.aviferdev.trackfolio.domain.usecase.fixedincome

import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.repository.FixedIncomeEventRepository
import kotlinx.datetime.Clock

class RegisterCouponUseCase(
    private val eventRepository: FixedIncomeEventRepository,
    private val recordIncomeTransactionUseCase: RecordIncomeTransactionUseCase
) {
    suspend operator fun invoke(event: FixedIncomeEvent): Result<Unit> {
        val eventResult = eventRepository.insert(event)
        if (eventResult.isFailure) return eventResult

        val ledgerResult = recordIncomeTransactionUseCase(
            accountId    = "",
            amount       = event.netAmount,
            date         = event.date,
            notes        = event.notes ?: "Cupón / Interés",
            linkedEventId = "fi_${event.id}"
        )

        return ledgerResult
    }
}

class RecordIncomeTransactionUseCase(
    private val transactionRepository: es.aviferdev.trackfolio.domain.repository.TransactionRepository
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