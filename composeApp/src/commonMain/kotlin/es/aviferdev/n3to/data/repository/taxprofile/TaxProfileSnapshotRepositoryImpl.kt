package es.aviferdev.n3to.data.repository.taxprofile

import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.data.datasource.taxprofile.TaxProfileSnapshotLocalDataSource
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.domain.model.TaxProfile
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.domain.repository.TaxProfileSnapshotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class TaxProfileSnapshotRepositoryImpl(
    private val dataSource: TaxProfileSnapshotLocalDataSource
) : TaxProfileSnapshotRepository {

    override fun getAll(): Flow<List<TaxProfileSnapshot>> = dataSource.getAll()

    override suspend fun getActive(date: LocalDate): TaxProfileSnapshot? =
        dataSource.getActive(date)

    override suspend fun save(profile: TaxProfile, effectiveFrom: LocalDate): Result<Unit> =
        dataSource.insert(
            id = uuid4().toString(),
            countryCode = profile.countryCode,
            currency = profile.currency,
            effectiveFrom = effectiveFrom,
            createdAt = nowMillis()
        )

    override suspend fun delete(id: String): Result<Unit> = dataSource.delete(id)
}
