package es.aviferdev.n3to.domain.usecase.assettransaction

import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.domain.repository.AssetTransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock

/**
 * Resultado de calcular el coste base arrastrado desde el fondo origen.
 *
 * @property costBasisPerUnit coste medio ponderado FIFO por participación
 *           del fondo origen. Se usará como `pricePerUnit` del TRANSFER_IN
 *           en el fondo destino.
 * @property totalCostBasis   coste total arrastrado = costBasisPerUnit × quantity.
 */
data class TransferCostBasis(
    val costBasisPerUnit: Double,
    val totalCostBasis: Double
)

/**
 * Ejecuta un traspaso entre fondos de inversión o planes de pensiones.
 *
 * Funcionamiento (idéntico al de los bancos españoles):
 * 1. Se crean dos AssetTransaction atómicas con el mismo `transferGroupId`:
 *    - **TRANSFER_OUT** en el fondo origen: resta participaciones sin generar P&L.
 *    - **TRANSFER_IN** en el fondo destino: suma participaciones con el coste
 *      base FIFO arrastrado del origen (sin hecho imponible).
 * 2. No se genera ningún movimiento en el libro de liquidez.
 * 3. El coste base del TRANSFER_IN se calcula como el coste medio ponderado
 *    FIFO de las participaciones que se consumirían del origen.
 * 4. Las participaciones destino = (coste total arrastrado) / (VL destino).
 */
class ExecuteFundTransferUseCase(
    private val repository: AssetTransactionRepository
) {

    /**
     * Calcula el coste base que se arrastraría al traspasar [quantity]
     * participaciones del fondo origen en la [platformId] dada.
     *
     * Útil para mostrar en la UI antes de confirmar el traspaso.
     */
    suspend fun calculateTransferCostBasis(
        sourceAssetId: String,
        platformId: String,
        quantity: Double
    ): TransferCostBasis {
        val txs = repository.getByAsset(sourceAssetId).firstOrNull() ?: emptyList()
        val costPerUnit = PortfolioCalculator.weightedCostBasisForTransfer(
            transactions = txs,
            platformId   = platformId,
            quantity      = quantity
        )
        return TransferCostBasis(
            costBasisPerUnit = costPerUnit,
            totalCostBasis   = costPerUnit * quantity
        )
    }

    /**
     * Ejecuta el traspaso completo.
     *
     * @param sourceAssetId     ID del activo de origen (fondo del que se sale).
     * @param destinationAssetId ID del activo destino (fondo al que se entra).
     * @param quantity           participaciones a traspasar del fondo origen.
     * @param sourcePlatformId   plataforma donde están las participaciones origen.
     * @param destinationPlatformId plataforma destino.
     * @param destinationPricePerUnit VL (valor liquidativo) del fondo destino a fecha
     *                               del traspaso. Las participaciones destino se calculan
     *                               como totalCostBasis / destinationPricePerUnit.
     * @param date               fecha del traspaso en epoch millis.
     *
     * @return [Result] con el par de transacciones creadas (OUT, IN).
     */
    suspend operator fun invoke(
        sourceAssetId: String,
        destinationAssetId: String,
        quantity: Double,
        sourcePlatformId: String,
        destinationPlatformId: String,
        destinationPricePerUnit: Double,
        date: Long
    ): Result<Pair<AssetTransaction, AssetTransaction>> = runCatching {
        val now = Clock.System.now().toEpochMilliseconds()
        val transferGroupId = "tf_${now}_${(0..9999).random()}"

        // 1. Calcular coste base arrastrado
        val costBasis = calculateTransferCostBasis(
            sourceAssetId = sourceAssetId,
            platformId    = sourcePlatformId,
            quantity      = quantity
        )

        // 2. Participaciones destino = coste total arrastrado / VL destino
        val destinationQuantity = costBasis.totalCostBasis / destinationPricePerUnit

        // 3. Coste por participación destino = coste total / participaciones destino
        //    Esto preserva el coste base total: destinationQuantity × costBasisPerUnitDest = totalCostBasis
        val costBasisPerUnitDest = costBasis.totalCostBasis / destinationQuantity

        // 4. Crear TRANSFER_OUT en origen
        val transferOut = AssetTransaction(
            id           = "txout_$transferGroupId",
            assetId      = sourceAssetId,
            type         = AssetTransactionType.TRANSFER_OUT,
            quantity     = quantity,
            pricePerUnit = costBasis.costBasisPerUnit,
            date         = date,
            platformId   = sourcePlatformId,
            feeNote      = null,
            notes        = "TRANSFER:$transferGroupId",
            createdAt    = now
        )

        // 5. Crear TRANSFER_IN en destino
        val transferIn = AssetTransaction(
            id           = "txin_$transferGroupId",
            assetId      = destinationAssetId,
            type         = AssetTransactionType.TRANSFER_IN,
            quantity     = destinationQuantity,
            pricePerUnit = costBasisPerUnitDest,
            date         = date,
            platformId   = destinationPlatformId,
            feeNote      = null,
            notes        = "TRANSFER:$transferGroupId",
            createdAt    = now + 1 // +1 para garantizar orden FIFO correcto
        )

        // 6. Persistir ambas transacciones
        repository.save(transferOut).getOrThrow()
        repository.save(transferIn).getOrThrow()

        transferOut to transferIn
    }
}
