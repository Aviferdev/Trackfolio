package es.aviferdev.n3to.domain.usecase.assettransaction

import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.TaxLine
import es.aviferdev.n3to.domain.model.TaxRole
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock

/**
 * Sincroniza movimientos de portfolio con el libro de liquidez.
 *
 * - BUY  → EXPENSE (reduce saldo de la cuenta)
 * - SELL → INCOME  (aumenta saldo de la cuenta)
 *
 * Cada [AssetTransaction] queda vinculada a una [Transaction] mediante
 * [Transaction.linkedAssetTransactionId]. Ese movimiento es de solo
 * lectura en el listado de transacciones.
 */
class SyncAssetTransactionToLedgerUseCase(
    private val transactionRepository: TransactionRepository
) {

    /**
     * Crea o actualiza la Transaction de liquidez vinculada a la inversión.
     */
    suspend fun sync(
        assetTx: AssetTransaction,
        accountId: String,
        assetName: String
    ): Result<Unit> {
        val txType = when (assetTx.type) {
            AssetTransactionType.BUY  -> TransactionType.EXPENSE
            AssetTransactionType.SELL -> TransactionType.INCOME
            // Los traspasos entre fondos no generan movimiento de liquidez
            AssetTransactionType.TRANSFER_OUT,
            AssetTransactionType.TRANSFER_IN  -> return Result.success(Unit)
        }
        val amount = assetTx.grossAmount
        val label  = when (assetTx.type) {
            AssetTransactionType.BUY  -> "Compra: ${fmtQty(assetTx.quantity)} uds. de $assetName"
            AssetTransactionType.SELL -> "Venta: ${fmtQty(assetTx.quantity)} uds. de $assetName"
            else -> return Result.success(Unit) // Nunca llega aquí, pero el compilador lo requiere
        }

        // Buscar si ya existe una Transaction vinculada
        val existing = transactionRepository
            .getByLinkedAssetTransaction(assetTx.id)
            .firstOrNull()

        return if (existing != null) {
            // Actualizar
            transactionRepository.updateTransaction(
                existing.copy(
                    amount = amount,
                    type   = txType,
                    date   = assetTx.date,
                    notes  = label
                )
            )
        } else {
            // Crear nueva
            val now = Clock.System.now().toEpochMilliseconds()
            val transaction = Transaction(
                id                       = "ledger_${assetTx.id}",
                accountId                = accountId,
                amount                   = amount,
                type                     = txType,
                categoryId               = null,
                date                     = assetTx.date,
                notes                    = label,
                createdAt                = now,
                linkedAssetTransactionId = assetTx.id
            )
            transactionRepository.saveTransaction(transaction)
        }
    }

    /**
     * Elimina la Transaction vinculada cuando se borra la AssetTransaction.
     */
    suspend fun remove(assetTransactionId: String): Result<Unit> =
        transactionRepository.deleteByLinkedAssetTransaction(assetTransactionId)

    /**
     * Crea o actualiza una Transaction de dividendo vinculada al activo.
     * El dividendo es un INCOME con datos fiscales (bruto, IRPF).
     *
     * @param dividendId ID único del dividendo (se usa como linkedAssetTransactionId)
     * @param accountId cuenta donde se registra
     * @param assetName nombre del activo para la nota descriptiva
     * @param grossAmount importe bruto del dividendo
     * @param irpfPercent porcentaje de retención IRPF aplicado
     * @param date fecha del dividendo en epoch millis
     * @param issuerId ID del emisor (acción)
     * @param issuerName nombre del emisor
     */
    suspend fun syncDividend(
        dividendId: String,
        accountId: String,
        assetName: String,
        grossAmount: Double,
        withholdingPercent: Double,
        date: Long,
        issuerId: String? = null,
        issuerName: String? = null
    ): Result<Unit> {
        val withholdingAmount = grossAmount * withholdingPercent / 100.0
        val netAmount = grossAmount - withholdingAmount
        val label = "Dividendo: $assetName"
        val taxLines = if (withholdingPercent > 0) listOf(
            TaxLine(name = "Retención", role = TaxRole.INCOME_TAX, percent = withholdingPercent, amount = withholdingAmount)
        ) else emptyList()

        val existing = transactionRepository
            .getByLinkedAssetTransaction(dividendId)
            .firstOrNull()

        return if (existing != null) {
            transactionRepository.updateTransaction(
                existing.copy(
                    amount      = netAmount,
                    date        = date,
                    notes       = label,
                    incomeType  = IncomeType.DIVIDEND,
                    grossAmount = grossAmount,
                    taxLines    = taxLines,
                    issuerId    = issuerId,
                    issuerName  = issuerName
                )
            )
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            val transaction = Transaction(
                id                       = "ledger_$dividendId",
                accountId                = accountId,
                amount                   = netAmount,
                type                     = TransactionType.INCOME,
                categoryId               = null,
                date                     = date,
                notes                    = label,
                createdAt                = now,
                incomeType               = IncomeType.DIVIDEND,
                grossAmount              = grossAmount,
                taxLines                 = taxLines,
                issuerId                 = issuerId,
                issuerName               = issuerName,
                linkedAssetTransactionId = dividendId
            )
            transactionRepository.saveTransaction(transaction)
        }
    }

    /**
     * Crea o actualiza una Transaction de rendimiento de bono/depósito vinculada al activo.
     * Similar a dividendo pero con campo de comisiones adicional.
     * Neto = bruto - IRPF - comisiones.
     */
    suspend fun syncBondDeposit(
        bondDepositId: String,
        accountId: String,
        assetName: String,
        grossAmount: Double,
        withholdingPercent: Double,
        commissionAmount: Double,
        date: Long,
        issuerId: String? = null,
        issuerName: String? = null
    ): Result<Unit> {
        val withholdingAmount = grossAmount * withholdingPercent / 100.0
        val netAmount = grossAmount - withholdingAmount - commissionAmount
        val label = "Rendimiento bono/depósito: $assetName"
        val taxLines = if (withholdingPercent > 0) listOf(
            TaxLine(name = "Retención", role = TaxRole.INCOME_TAX, percent = withholdingPercent, amount = withholdingAmount)
        ) else emptyList()

        val existing = transactionRepository
            .getByLinkedAssetTransaction(bondDepositId)
            .firstOrNull()

        return if (existing != null) {
            transactionRepository.updateTransaction(
                existing.copy(
                    amount           = netAmount,
                    date             = date,
                    notes            = label,
                    incomeType       = IncomeType.BOND_DEPOSIT,
                    grossAmount      = grossAmount,
                    taxLines         = taxLines,
                    commissionAmount = commissionAmount,
                    issuerId         = issuerId,
                    issuerName       = issuerName
                )
            )
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            val transaction = Transaction(
                id                       = "ledger_$bondDepositId",
                accountId                = accountId,
                amount                   = netAmount,
                type                     = TransactionType.INCOME,
                categoryId               = null,
                date                     = date,
                notes                    = label,
                createdAt                = now,
                incomeType               = IncomeType.BOND_DEPOSIT,
                grossAmount              = grossAmount,
                taxLines                 = taxLines,
                commissionAmount         = commissionAmount,
                issuerId                 = issuerId,
                issuerName               = issuerName,
                linkedAssetTransactionId = bondDepositId
            )
            transactionRepository.saveTransaction(transaction)
        }
    }

    private fun fmtQty(v: Double): String {
        return if (v == v.toLong().toDouble()) v.toLong().toString()
        else v.toString().replace('.', ',')
    }
}
