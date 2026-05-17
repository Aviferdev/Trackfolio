package es.aviferdev.n3to.domain.usecase.taxprofile

import es.aviferdev.n3to.domain.model.TaxProfile
import es.aviferdev.n3to.domain.repository.TaxProfileSnapshotRepository
import kotlinx.datetime.LocalDate

class SaveTaxProfileSnapshotUseCase(private val repository: TaxProfileSnapshotRepository) {
    suspend operator fun invoke(profile: TaxProfile, effectiveFrom: LocalDate): Result<Unit> =
        repository.save(profile, effectiveFrom)
}
