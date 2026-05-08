package es.aviferdev.trackfolio.domain.usecase.fixedincome

import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.model.FixedIncomePosition
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Transaction
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.domain.repository.FixedIncomeEventRepository
import es.aviferdev.trackfolio.domain.repository.FixedIncomeRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CreateFixedIncomePositionUseCase(
    private val positionRepository: FixedIncomeRepository,
    private val eventRepository: FixedIncomeEventRepository,
    private val createLedgerTransactionUseCase: CreateLedgerTransactionUseCase
) {
    suspend operator fun invoke(
        position: FixedIncomePosition,
        acquisitionEvent: FixedIncomeEvent
    ): Result<Unit> {
        val posResult = positionRepository.insert(position)
        if (posResult.isFailure) return posResult

        val eventResult = eventRepository.insert(acquisitionEvent)
        if (eventResult.isFailure) return eventResult

        val ledgerResult = createLedgerTransactionUseCase(
            accountId    = position.accountId,
            amount       = position.principal,
            type         = TransactionType.EXPENSE,
            date         = acquisitionEvent.date,
            incomeTypeId = null,
            notes        = "Adquisición: ${position.name}",
            linkedEventId = "fi_${acquisitionEvent.id}"
        )

        return ledgerResult
    }
}

class CreateLedgerTransactionUseCase(
    private val transactionRepository: es.aviferdev.trackfolio.domain.repository.TransactionRepository
) {
    suspend operator fun invoke(
        accountId: String,
        amount: Double,
        type: TransactionType,
        date: Long,
        incomeTypeId: String?,
        notes: String?,
        linkedEventId: String?
    ): Result<Unit> = runCatching {
        val tx = Transaction(
            id                  = "tx_${Clock.System.now().toEpochMilliseconds()}",
            accountId           = accountId,
            amount              = amount,
            type                = type,
            date                = date,
            categoryId          = null,
            incomeType          = incomeTypeId?.let { IncomeType.fromName(it) },
            notes               = notes,
            linkedAssetTransactionId = linkedEventId,
            createdAt           = Clock.System.now().toEpochMilliseconds()
        )
        transactionRepository.saveTransaction(tx)
    }
}