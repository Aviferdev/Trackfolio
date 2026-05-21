package es.aviferdev.n3to.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.SatelliteAlt
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Work
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
    "₿" to Icons.Outlined.CurrencyBitcoin,
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
    "🛡" to Icons.Outlined.Shield,
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
    "⚖" to Icons.Outlined.Balance,
    "❔" to Icons.AutoMirrored.Outlined.HelpOutline,
    "🌍" to Icons.Outlined.Public,
)

/**
 * Convierte un emoji String al [ImageVector] de Material correspondiente.
 * Si no hay mapeo, devuelve [Icons.Outlined.HelpOutline].
 */
fun String.toMaterialIcon(): ImageVector =
    EMOJI_TO_ICON[this] ?: Icons.AutoMirrored.Outlined.HelpOutline

// ─────────────────────────────────────────────────────────────────────────────
//  Extension functions for domain enums
//  Evitan que la capa de dominio dependa de Compose.
// ─────────────────────────────────────────────────────────────────────────────

fun IncomeType.toMaterialIcon(): ImageVector = when (this) {
    IncomeType.SALARY -> Icons.Outlined.Work
    IncomeType.BANK_INTEREST -> Icons.Outlined.AccountBalance
    IncomeType.BOND_DEPOSIT -> Icons.AutoMirrored.Outlined.ReceiptLong
    IncomeType.DIVIDEND -> Icons.AutoMirrored.Outlined.ShowChart
    IncomeType.BONUS_PRIZE -> Icons.Outlined.CardGiftcard
    IncomeType.PRIZE_LOTTERY -> Icons.Outlined.EmojiEvents
    IncomeType.RENTAL_INCOME -> Icons.Outlined.House
    IncomeType.FREELANCE -> Icons.Outlined.BusinessCenter
    IncomeType.EXEMPT_INCOME -> Icons.AutoMirrored.Outlined.Assignment
}

fun FixedIncomeType.toMaterialIcon(): ImageVector = when (this) {
    FixedIncomeType.BILL -> Icons.AutoMirrored.Outlined.Assignment
    FixedIncomeType.BOND -> Icons.AutoMirrored.Outlined.ReceiptLong
    FixedIncomeType.GOVERNMENT_OBLIGATION -> Icons.Outlined.AccountBalance
    FixedIncomeType.CORPORATE_BOND -> Icons.Outlined.Business
    FixedIncomeType.DEPOSIT -> Icons.Outlined.Savings
}

fun LoanType.toMaterialIcon(): ImageVector = when (this) {
    LoanType.MORTGAGE -> Icons.Outlined.House
    LoanType.CAR -> Icons.Outlined.DirectionsCar
    LoanType.STUDENT -> Icons.Outlined.School
    LoanType.PERSONAL -> Icons.Outlined.Payments
    LoanType.OTHER -> Icons.Outlined.MoreHoriz
}
