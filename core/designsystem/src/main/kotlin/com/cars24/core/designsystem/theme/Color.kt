package com.cars24.core.designsystem.theme

import androidx.compose.ui.graphics.Color

internal val Indigo900 = Color(0xFF11144B)
internal val Indigo800 = Color(0xFF1B2065)
internal val Indigo600 = Color(0xFF2E3A8C)
internal val Indigo400 = Color(0xFF5865C4)
internal val Indigo100 = Color(0xFFE3E6F7)

internal val Amber600 = Color(0xFFE8890C)
internal val Amber500 = Color(0xFFFFA724)
internal val Amber100 = Color(0xFFFFF1DC)

internal val Teal600 = Color(0xFF0B8A6B)
internal val Teal100 = Color(0xFFDDF4EE)

internal val Red600 = Color(0xFFC0392B)
internal val Red100 = Color(0xFFFDE7E4)

internal val Neutral0 = Color(0xFFFFFFFF)
internal val Neutral50 = Color(0xFFF6F7FB)
internal val Neutral100 = Color(0xFFEDEFF5)
internal val Neutral200 = Color(0xFFDFE3EC)
internal val Neutral400 = Color(0xFF9AA1B4)
internal val Neutral600 = Color(0xFF616B84)
internal val Neutral800 = Color(0xFF2B3247)
internal val Neutral900 = Color(0xFF151A28)

internal val SavedHeartRed = Color(0xFFFF4D5E)

internal val OnBrandMuted = Color(0xFFFFFFFF).copy(alpha = 0.75f)
internal val OnBrandSubtle = Color(0xFFFFFFFF).copy(alpha = 0.22f)
internal val PhotoScrim = Color(0xFF000000).copy(alpha = 0.22f)
internal val PhotoBadgeSurface = Color(0xFFFFFFFF).copy(alpha = 0.92f)

internal val Dark0 = Color(0xFF0B0E19)
internal val Dark50 = Color(0xFF131728)
internal val Dark100 = Color(0xFF1B2136)
internal val Dark200 = Color(0xFF262E48)

data class Cars24Colors(
    val brandGradient: List<Color>,
    val price: Color,
    val accent: Color,
    val accentContainer: Color,
    val success: Color,
    val successContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val cardSurface: Color,
    val pageBackground: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val shimmer: List<Color>,
    val onBrand: Color,
    val onBrandMuted: Color,
    val onBrandSubtle: Color,
    val photoScrim: Color,
    val photoBadgeSurface: Color,
    val savedHeart: Color,
    val isDark: Boolean,
)

internal val LightCars24Colors = Cars24Colors(
    brandGradient = listOf(Indigo900, Indigo600),
    price = Neutral900,
    accent = Amber600,
    accentContainer = Amber100,
    success = Teal600,
    successContainer = Teal100,
    danger = Red600,
    dangerContainer = Red100,
    cardSurface = Neutral0,
    pageBackground = Neutral50,
    divider = Neutral200,
    textPrimary = Neutral900,
    textSecondary = Neutral600,
    textTertiary = Neutral400,
    shimmer = listOf(Neutral100, Neutral200, Neutral100),
    onBrand = Neutral0,
    onBrandMuted = OnBrandMuted,
    onBrandSubtle = OnBrandSubtle,
    photoScrim = PhotoScrim,
    photoBadgeSurface = PhotoBadgeSurface,
    savedHeart = SavedHeartRed,
    isDark = false,
)

internal val DarkCars24Colors = Cars24Colors(
    brandGradient = listOf(Dark0, Indigo800),
    price = Neutral0,
    accent = Amber500,
    accentContainer = Color(0xFF3A2A0E),
    success = Color(0xFF3FCFA8),
    successContainer = Color(0xFF0E3129),
    danger = Color(0xFFFF7A6B),
    dangerContainer = Color(0xFF3A1A16),
    cardSurface = Dark100,
    pageBackground = Dark0,
    divider = Dark200,
    textPrimary = Color(0xFFF2F4FA),
    textSecondary = Color(0xFFA8B0C6),
    textTertiary = Color(0xFF6C7590),
    shimmer = listOf(Dark100, Dark200, Dark100),
    onBrand = Neutral0,
    onBrandMuted = OnBrandMuted,
    onBrandSubtle = OnBrandSubtle,
    photoScrim = PhotoScrim,
    photoBadgeSurface = PhotoBadgeSurface,
    savedHeart = SavedHeartRed,
    isDark = true,
)

val CarPhotoPlaceholderPalettes: List<List<Color>> = listOf(
    listOf(Color(0xFF3E4A8A), Color(0xFF7A86C7)),
    listOf(Color(0xFF1F6F63), Color(0xFF63BFAE)),
    listOf(Color(0xFF7A4A1C), Color(0xFFD79B5A)),
    listOf(Color(0xFF4A2A5E), Color(0xFF9A76B4)),
    listOf(Color(0xFF25405E), Color(0xFF6D93B8)),
    listOf(Color(0xFF5E2A34), Color(0xFFB4747F)),
    listOf(Color(0xFF2F5E3A), Color(0xFF77B487)),
    listOf(Color(0xFF6B2F4E), Color(0xFFC17FA0)),
    listOf(Color(0xFF404A55), Color(0xFF8D9AA8)),
    listOf(Color(0xFF1B5C6E), Color(0xFF63A9BC)),
)

object PromoGradients {
    val ZeroDownPayment = listOf(Color(0xFF1B2065), Color(0xFF5865C4))
    val SellInOneVisit = listOf(Color(0xFF0B8A6B), Color(0xFF3FCFA8))
    val MoneyBack = listOf(Color(0xFFE8890C), Color(0xFFFFC46B))
    val SellFooter = listOf(Color(0xFF11144B), Color(0xFF2E3A8C))
}
