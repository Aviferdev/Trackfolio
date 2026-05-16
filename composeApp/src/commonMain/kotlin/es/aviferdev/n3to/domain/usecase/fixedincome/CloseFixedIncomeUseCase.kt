package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.domain.model.FixedIncomeCloseType
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.model.FixedIncomeEventType
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository
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
        settlementEvent: FixedIncomeEvent,
        accountId: String
    ): Result<Unit> {
        val closeResult = positionRepository.close(positionId, closeDate, closeType.name)
        if (closeResult.isFailure) return closeResult

        val eventResult = eventRepository.insert(settlementEvent)
        if (eventResult.isFailure) return eventResult

        val ledgerResult = recordSettlementTransactionUseCase(
            accountId    = accountId,
            amount       = settlementEvent.netAmount,
            date         = settlementEvent.date,
            notes        = "Liquidación renta fija: ${settlementEvent.type.label}",
            linkedEventId = "fi_${settlementEvent.id}"
        )

        return ledgerResult
    }
}

class RecordSettlementTransactionUseCase(
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