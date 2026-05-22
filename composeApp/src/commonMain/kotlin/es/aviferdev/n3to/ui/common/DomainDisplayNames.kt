package es.aviferdev.n3to.ui.common

/**
 * Domain model display names – @Composable functions that resolve enum values
 * to localized strings via Compose Multiplatform Resources.
 *
 * Each domain model enum has a [resourceKey] property that maps to a [Res.string.*]
 * resource. These extension functions provide the idiomatic way to get a human-readable
 * label for any supported enum value.
 *
 * NOTE: The [label] property on domain enums is a non-localized Spanish fallback.
 * Always prefer [displayName()] in @Composable contexts.
 */
import androidx.compose.runtime.Composable
import es.aviferdev.n3to.domain.model.FixedIncomeCategory
import es.aviferdev.n3to.domain.model.FixedIncomeEventType
import es.aviferdev.n3to.domain.model.FixedIncomeType
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.InterestFrequency
import es.aviferdev.n3to.domain.model.IssuerType
import es.aviferdev.n3to.domain.model.LoanType
import es.aviferdev.n3to.domain.model.PropertyType
import es.aviferdev.n3to.domain.model.RentalStatus
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun IncomeType.displayName(): String = stringResource(resourceKeyFor(this))

@Composable
fun FixedIncomeType.displayName(): String = stringResource(resourceKeyFor(this))

@Composable
fun FixedIncomeCategory.displayName(): String = stringResource(resourceKeyFor(this))

@Composable
fun FixedIncomeEventType.displayName(): String = stringResource(resourceKeyFor(this))

@Composable
fun InterestFrequency.displayName(): String = stringResource(resourceKeyFor(this))

@Composable
fun LoanType.displayName(): String = stringResource(resourceKeyFor(this))

@Composable
fun PropertyType.displayName(): String = stringResource(resourceKeyFor(this))

@Composable
fun RentalStatus.displayName(): String = stringResource(resourceKeyFor(this))

@Composable
fun IssuerType.displayName(): String = stringResource(resourceKeyFor(this))

private fun resourceKeyFor(type: IncomeType): StringResource = when (type) {
    IncomeType.SALARY -> Res.string.income_type_salary
    IncomeType.FREELANCE -> Res.string.income_type_freelance
    IncomeType.BANK_INTEREST -> Res.string.income_type_bank_interest
    IncomeType.BOND_DEPOSIT -> Res.string.income_type_bond_deposit
    IncomeType.DIVIDEND -> Res.string.income_type_dividend
    IncomeType.BONUS_PRIZE -> Res.string.income_type_bonus_prize
    IncomeType.PRIZE_LOTTERY -> Res.string.income_type_prize_lottery
    IncomeType.RENTAL_INCOME -> Res.string.income_type_rental_income
    IncomeType.EXEMPT_INCOME -> Res.string.income_type_exempt_income
}

private fun resourceKeyFor(type: FixedIncomeType): StringResource = when (type) {
    FixedIncomeType.BILL -> Res.string.fixedincome_type_bill
    FixedIncomeType.BOND -> Res.string.fixedincome_type_bond
    FixedIncomeType.GOVERNMENT_OBLIGATION -> Res.string.fixedincome_type_government_obligation
    FixedIncomeType.CORPORATE_BOND -> Res.string.fixedincome_type_corporate_bond
    FixedIncomeType.DEPOSIT -> Res.string.fixedincome_type_deposit
}

private fun resourceKeyFor(category: FixedIncomeCategory): StringResource = when (category) {
    FixedIncomeCategory.GOVERNMENT -> Res.string.fixedincome_category_government
    FixedIncomeCategory.CORPORATE -> Res.string.fixedincome_category_corporate
    FixedIncomeCategory.DEPOSIT -> Res.string.fixedincome_category_deposit
}

private fun resourceKeyFor(event: FixedIncomeEventType): StringResource = when (event) {
    FixedIncomeEventType.ACQUISITION -> Res.string.fixedincome_event_acquisition
    FixedIncomeEventType.COUPON -> Res.string.fixedincome_event_coupon
    FixedIncomeEventType.MATURITY_SETTLEMENT -> Res.string.fixedincome_event_maturity_settlement
    FixedIncomeEventType.SECONDARY_SALE -> Res.string.fixedincome_event_secondary_sale
    FixedIncomeEventType.EARLY_CANCELLATION -> Res.string.fixedincome_event_early_cancellation
    FixedIncomeEventType.PARTIAL_AMORTIZATION -> Res.string.fixedincome_event_partial_amortization
}

private fun resourceKeyFor(frequency: InterestFrequency): StringResource = when (frequency) {
    InterestFrequency.AT_MATURITY -> Res.string.interest_frequency_at_maturity
    InterestFrequency.MONTHLY -> Res.string.interest_frequency_monthly
    InterestFrequency.QUARTERLY -> Res.string.interest_frequency_quarterly
    InterestFrequency.SEMIANNUAL -> Res.string.interest_frequency_semiannual
    InterestFrequency.ANNUAL -> Res.string.interest_frequency_annual
}

private fun resourceKeyFor(type: LoanType): StringResource = when (type) {
    LoanType.MORTGAGE -> Res.string.loan_type_mortgage
    LoanType.CAR -> Res.string.loan_type_car
    LoanType.STUDENT -> Res.string.loan_type_student
    LoanType.PERSONAL -> Res.string.loan_type_personal
    LoanType.OTHER -> Res.string.loan_type_other
}

private fun resourceKeyFor(type: PropertyType): StringResource = when (type) {
    PropertyType.PRIMARY_HOME -> Res.string.property_type_primary_home
    PropertyType.SECONDARY_HOME -> Res.string.property_type_secondary_home
    PropertyType.INVESTMENT -> Res.string.property_type_investment
}

private fun resourceKeyFor(status: RentalStatus): StringResource = when (status) {
    RentalStatus.OWN_USE -> Res.string.rental_status_own_use
    RentalStatus.RENTED -> Res.string.rental_status_rented
    RentalStatus.VACANT -> Res.string.rental_status_vacant
}

private fun resourceKeyFor(type: IssuerType): StringResource = when (type) {
    IssuerType.EMPLOYER -> Res.string.issuer_type_employer
    IssuerType.BANK -> Res.string.issuer_type_bank
    IssuerType.BOND_ISSUER -> Res.string.issuer_type_bond_issuer
    IssuerType.DIVIDEND_SOURCE -> Res.string.issuer_type_dividend_source
    IssuerType.PROMOTION_PLATFORM -> Res.string.issuer_type_promotion_platform
    IssuerType.EXEMPT_SOURCE -> Res.string.issuer_type_exempt_source
}
