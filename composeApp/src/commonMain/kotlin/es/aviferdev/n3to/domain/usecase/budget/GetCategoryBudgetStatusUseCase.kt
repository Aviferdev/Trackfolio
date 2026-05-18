package es.aviferdev.n3to.domain.usecase.budget

import es.aviferdev.n3to.domain.model.CategoryBudgetStatus
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.repository.CategoryBudgetRepository
import es.aviferdev.n3to.domain.repository.CategoryRepository
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull

class GetCategoryBudgetStatusUseCase(
    private val budgetRepository: CategoryBudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(accountId: String, year: String): Flow<List<CategoryBudgetStatus>> {
        if (accountId.isBlank()) return emptyFlow()

        return combine(
            budgetRepository.getBudgetsByAccount(accountId),
            transactionRepository.getExpensesByCategoryPerYear(accountId, year),
            transactionRepository.getAnnualSummaryByAccount(accountId, year)
        ) { budgets, expenses, annualSummary ->

            val totalIncome = annualSummary.totalIncome
            val expenseByCategoryId = expenses.associateBy { it.categoryId }

            // Necesitamos nombres de categorías. Los obtenemos del datasource si no se pasan ya
            val categoryIds = budgets.map { it.categoryId }
            val categoryNames = mutableMapOf<String, String>()

            // Cargar nombres con el repositorio (una sola vez)
            val allCategories = categoryRepository
                .getByTypeAndAccount(accountId, TransactionType.EXPENSE)
                .firstOrNull()
                ?.associate { it.id to it.name } ?: emptyMap()

            budgets.mapNotNull { budget ->
                val spent = expenseByCategoryId[budget.categoryId]?.amount ?: 0.0
                val name = categoryNames[budget.categoryId]
                    ?: allCategories[budget.categoryId]
                    ?: "Sin categoría"

                if (budget.annualLimit <= 0.0) null
                else CategoryBudgetStatus(
                    categoryId = budget.categoryId,
                    categoryName = name,
                    annualLimit = budget.annualLimit,
                    limitType = try {
                        LimitType.valueOf(budget.limitType)
                    } catch (_: IllegalArgumentException) {
                        LimitType.FIXED
                    },
                    spent = spent,
                    totalIncome = totalIncome,
                    year = year
                )
            }
        }
    }
}
