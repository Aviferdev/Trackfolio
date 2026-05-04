package es.aviferdev.trackfolio.domain.usecase.account

import es.aviferdev.trackfolio.data.database.DatabaseInitializer
import es.aviferdev.trackfolio.data.datasource.AccountLocalDataSource

class SetInitialBalanceUseCase(
    private val dataSource: AccountLocalDataSource
) {
    suspend operator fun invoke(balance: Double): Result<Unit> =
        dataSource.updateBalance(
            id = DatabaseInitializer.DEFAULT_ACCOUNT_ID,
            balance = balance
        )
}
