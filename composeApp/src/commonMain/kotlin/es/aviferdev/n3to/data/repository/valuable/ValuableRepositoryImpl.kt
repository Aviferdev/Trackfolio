package es.aviferdev.n3to.data.repository.valuable

import es.aviferdev.n3to.data.datasource.valuable.ValuableLocalDataSource
import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.repository.ValuableRepository
import kotlinx.coroutines.flow.Flow

class ValuableRepositoryImpl(
    private val localDataSource: ValuableLocalDataSource
) : ValuableRepository {

    override fun getByAccount(accountId: String): Flow<List<Valuable>> =
        localDataSource.getByAccount(accountId)

    override fun getActiveByAccount(accountId: String): Flow<List<Valuable>> =
        localDataSource.getActiveByAccount(accountId)

    override fun getById(id: String): Flow<Valuable?> =
        localDataSource.getById(id)

    override suspend fun save(valuable: Valuable): Result<Unit> =
        localDataSource.insert(valuable)

    override suspend fun sell(id: String, saleDate: Long, salePrice: Double, closeType: String): Result<Unit> =
        localDataSource.sell(id, saleDate, salePrice, closeType)

    override suspend fun updateEstimatedValue(id: String, value: Double): Result<Unit> =
        localDataSource.updateEstimatedValue(id, value)

    override suspend fun linkLoan(valuableId: String, loanId: String): Result<Unit> =
        localDataSource.updateLinkedLoan(valuableId, loanId)

    override suspend fun archive(id: String): Result<Unit> =
        localDataSource.archive(id)

    override suspend fun unarchive(id: String): Result<Unit> =
        localDataSource.unarchive(id)

    override suspend fun delete(id: String): Result<Unit> =
        localDataSource.delete(id)
}
