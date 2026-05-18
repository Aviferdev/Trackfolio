package es.aviferdev.n3to.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import es.aviferdev.n3to.domain.model.FixedIncomeType
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.LoanType

// ─────────────────────────────────────────────────────────────────────────────
//  General emoji → ImageVector mapper
//  Mantiene compatibilidad con la BD: los iconos se siguen guardando como
//  emoji String, pero se renderizan como Material icons.
// ─────────────────────────────────────────────────────────────────────────────

private val EMOJI_TO_ICON: Map<String, ImageVector> = mapOf(
    // ── Banks / Platforms ────────────────────────────────────────────────
    "🏦" to Icons.Outlined.AccountBalance,
    "💼" to Icons.Outlined.BusinessCenter,
    "💱" to Icons.Outlined.CurrencyExchange,
    "📈" to Icons.AutoMirrored.Outlined.ShowChart,
    "💳" to Icons.Outlined.CreditCard,
    "🏢" to Icons.Outlined.Business,
    "₿"  to Icons.Outlined.CurrencyBitcoin,
    "🪙" to Icons.Outlined.MonetizationOn,
    "💰" to Icons.Outlined.Savings,
    "🌐" to Icons.Outlined.Language,
    "📊" to Icons.Outlined.BarChart,
    "🛡️" to Icons.Outlined.Shield,
    "🎯" to Icons.Outlined.GpsFixed,
    "🔐" to Icons.Outlined.Lock,
    "📱" to Icons.Outlined.PhoneAndroid,
    "💻" to Icons.Outlined.Laptop,
    "🚀" to Icons.Outlined.RocketLaunch,
    "⚖️" to Icons.Outlined.Balance,

    // ── Asset Categories (DatabaseInitializer) ───────────────────────────
    "🛡"  to Icons.Outlined.Shield,
    "🤝" to Icons.Outlined.Handshake,
    "📦" to Icons.Outlined.Inventory2,

    // ── Income / Fixed Income / Loans ──────────────────────────────────
    "📜" to Icons.AutoMirrored.Outlined.ReceiptLong,
    "🎁" to Icons.Outlined.CardGiftcard,
    "📋" to Icons.AutoMirrored.Outlined.Assignment,
    "🏛️" to Icons.Outlined.AccountBalance,
    "🏠" to Icons.Outlined.House,
    "🚗" to Icons.Outlined.DirectionsCar,
    "🎓" to Icons.Outlined.School,

    // ── Sectors (SectorManagementSheet) ─────────────────────────────────
    "🏥" to Icons.Outlined.LocalHospital,
    "⚡" to Icons.Outlined.ElectricBolt,
    "🛒" to Icons.Outlined.ShoppingCart,
    "🏭" to Icons.Outlined.Factory,
    "📡" to Icons.Outlined.SatelliteAlt,
    "💎" to Icons.Outlined.Diamond,
    "💡" to Icons.Outlined.Lightbulb,
    "🎮" to Icons.Outlined.SportsEsports,
    "🍔" to Icons.Outlined.Fastfood,
    "💊" to Icons.Outlined.Medication,
    "📚" to Icons.AutoMirrored.Outlined.MenuBook,
    "🎬" to Icons.Outlined.Movie,
    "🔧" to Icons.Outlined.Build,
    "🌾" to Icons.Outlined.Agriculture,

    // ── Sector extras (DatabaseInitializer) ──────────────────────────────
    "💎" to Icons.Outlined.Diamond,
    "🍔" to Icons.Outlined.Fastfood,

    // ── UI-only emojis ──────────────────────────────────────────────────
    "📉" to Icons.AutoMirrored.Outlined.TrendingDown,
    "💾" to Icons.Outlined.SaveAlt,
    "🔄" to Icons.Outlined.Sync,
    "⚖"  to Icons.Outlined.Balance,
    "❔" to Icons.AutoMirrored.Outlined.HelpOutline,
    "🌍" to Icons.Outlined.Public,
)

/**
 * Convierte un emoji String al [ImageVector] de Material correspondiente.
 * Si no hay mapeo, devuelve [Icons.Outlined.HelpOutline].
 */
fun String.toMaterialIcon(): ImageVector = EMOJI_TO_ICON[this] ?: Icons.AutoMirrored.Outlined.HelpOutline

// ─────────────────────────────────────────────────────────────────────────────
//  Extension functions for domain enums
//  Evitan que la capa de dominio dependa de Compose.
// ─────────────────────────────────────────────────────────────────────────────

fun IncomeType.toMaterialIcon(): ImageVector = when (this) {
    IncomeType.SALARY           -> Icons.Outlined.Work
    IncomeType.BANK_INTEREST    -> Icons.Outlined.AccountBalance
    IncomeType.BOND_DEPOSIT     -> Icons.AutoMirrored.Outlined.ReceiptLong
    IncomeType.DIVIDEND         -> Icons.AutoMirrored.Outlined.ShowChart
    IncomeType.BONUS_PRIZE      -> Icons.Outlined.CardGiftcard
    IncomeType.PRIZE_LOTTERY    -> Icons.Outlined.EmojiEvents
    IncomeType.RENTAL_INCOME    -> Icons.Outlined.House
    IncomeType.FREELANCE        -> Icons.Outlined.BusinessCenter
    IncomeType.EXEMPT_INCOME    -> Icons.AutoMirrored.Outlined.Assignment
}

fun FixedIncomeType.toMaterialIcon(): ImageVector = when (this) {
    FixedIncomeType.BILL                  -> Icons.AutoMirrored.Outlined.Assignment
    FixedIncomeType.BOND                  -> Icons.AutoMirrored.Outlined.ReceiptLong
    FixedIncomeType.GOVERNMENT_OBLIGATION -> Icons.Outlined.AccountBalance
    FixedIncomeType.CORPORATE_BOND        -> Icons.Outlined.Business
    FixedIncomeType.DEPOSIT               -> Icons.Outlined.Savings
}

fun LoanType.toMaterialIcon(): ImageVector = when (this) {
    LoanType.MORTGAGE  -> Icons.Outlined.House
    LoanType.CAR       -> Icons.Outlined.DirectionsCar
    LoanType.STUDENT   -> Icons.Outlined.School
    LoanType.PERSONAL  -> Icons.Outlined.Payments
    LoanType.OTHER     -> Icons.Outlined.MoreHoriz
}
