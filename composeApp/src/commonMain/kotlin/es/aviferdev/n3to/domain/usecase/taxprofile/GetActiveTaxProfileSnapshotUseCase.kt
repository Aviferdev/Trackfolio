package es.aviferdev.n3to.domain.usecase.taxprofile

import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.domain.repository.TaxProfileSnapshotRepository
import kotlinx.datetime.LocalDate

class GetActiveTaxProfileSnapshotUseCase(private val repository: TaxProfileSnapshotRepository) {
    suspend operator fun invoke(date: LocalDate): TaxProfileSnapshot? =
        repository.getActive(date)
}
