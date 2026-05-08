package es.aviferdev.trackfolio.domain.usecase.fixedincome

import es.aviferdev.trackfolio.domain.model.FixedIncomeCloseType
import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.model.FixedIncomeEventType
import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.repository.FixedIncomeEventRepository
import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository
import kotlinx.datetime.Clock

class CloseFixedIncomeUseCase(
    private val positionRepository: FixedIncomeRepository,
    private val eventRepository: FixedIncomeEventRepository,
    private val recordSettlementTransactionUseCase: RecordSettlementTransactionUseCase
) {
    suspend operator fun invoke(
        positionId: String,
        closeType: FixedIncomeCloseType,
        closeDate: Long,
        settlementEvent: FixedIncomeEvent
    ): Result<Unit> {
        val closeResult = positionRepository.close(positionId, closeDate, closeType.name)
        if (closeResult.isFailure) return closeResult

        val eventResult = eventRepository.insert(settlementEvent)
        if (eventResult.isFailure) return eventResult

        val ledgerResult = recordSettlementTransactionUseCase(
            accountId    = "",
            amount       = settlementEvent.netAmount,
            date         = settlementEvent.date,
            notes        = "Liquidación renta fija: ${settlementEvent.type.label}",
            linkedEventId = "fi_${settlementEvent.id}"
        )

        return ledgerResult
    }
}

class RecordSettlementTransactionUseCase(
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