package es.aviferdev.n3to.domain.usecase.reconciliation

import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.data.database.DatabaseInitializer
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.TransactionRepository
import es.aviferdev.n3to.core.security.AppSettings
import es.aviferdev.n3to.ui.theme.formatAmount
import kotlinx.datetime.Clock

/**
 * Reconcilia el saldo de una cuenta de efectivo.
 * Calcula la diferencia entre saldo calculado y saldo real,
 * y crea una transacción ADJUSTMENT automática.
 *
 * @return Result con la transacción creada, o failure si el saldo ya cuadra.
 */
class ReconcileBalanceUseCase(
    private val transactionRepository: TransactionRepository,
    private val appSettings: AppSettings
) {
    suspend operator fun invoke(
        accountId: String,
        computedBalance: Double,
        realBalance: Double
    ): Result<Transaction> {
        val difference = realBalance - computedBalance

        if (kotlin.math.abs(difference) < 0.01) {
            return Result.failure(BalanceAlreadyMatchesException())
        }

        val now = Clock.System.now().toEpochMilliseconds()

        val formattedExpected = formatAmount(computedBalance)
        val formattedReal     = formatAmount(realBalance)
        val formattedDiff     = if (difference > 0) "+${formatAmount(difference)}" else formatAmount(difference)

        val transaction = Transaction(
            id                = uuid4().toString(),
            accountId         = accountId,
            amount            = difference, // positivo suma, negativo resta
            type              = TransactionType.ADJUSTMENT,
            categoryId        = DatabaseInitializer.ADJUSTMENT_CATEGORY_ID,
            date              = now,
            notes             = "Reconciliación: esperado ${formattedExpected}€, real ${formattedReal}€ (${formattedDiff}€)",
            createdAt         = now,
            excludeFromFiscal = true
        )

        val result = transactionRepository.saveTransaction(transaction)

        if (result.isSuccess) {
            appSettings.putLong(KEY_LAST_RECONCILIATION_DATE, now)
        }

        return result.map { transaction }
    }

    companion object {
        const val KEY_LAST_RECONCILIATION_DATE = "last_reconciliation_date"
    }
}

class BalanceAlreadyMatchesException : Exception("El saldo ya está cuadrado")
