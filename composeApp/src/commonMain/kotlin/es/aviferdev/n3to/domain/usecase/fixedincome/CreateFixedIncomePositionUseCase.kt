package es.aviferdev.n3to.domain.usecase.fixedincome

import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.FixedIncomeEventRepository
import es.aviferdev.n3to.domain.repository.FixedIncomeRepository

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
    private val transactionRepository: es.aviferdev.n3to.domain.repository.TransactionRepository
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
            id                  = "tx_${nowMillis()}",
            accountId           = accountId,
            amount              = amount,
            type                = type,
            date                = date,
            categoryId          = null,
            incomeType          = incomeTypeId?.let { IncomeType.fromName(it) },
            notes               = notes,
            linkedAssetTransactionId = linkedEventId,
            createdAt           = nowMillis()
        )
        transactionRepository.saveTransaction(tx)
    }
}