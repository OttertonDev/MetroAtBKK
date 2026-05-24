package com.ottertondev.metroatbkk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ottertondev.metroatbkk.R

val GoogleSansFlexWeight1000 = FontWeight(1000)

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlex = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight)
        )
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Bold.weight)
        )
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Black.weight)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlexHeader = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = GoogleSansFlexWeight1000,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(1000)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlexBodyMain = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = GoogleSansFlexWeight1000,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(1000),
            FontVariation.Setting("ROND", 11f),
            FontVariation.width(25f)
        )
    )
)

private fun googleSansFlexTextStyle(
    fontWeight: FontWeight,
    fontSize: Int,
    lineHeight: Int
): TextStyle {
    return TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = fontWeight,
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = 0.sp
    )
}

val Typography = Typography(
    displayLarge = googleSansFlexTextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 57,
        lineHeight = 64
    ),
    displayMedium = googleSansFlexTextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 45,
        lineHeight = 52
    ),
    displaySmall = googleSansFlexTextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 36,
        lineHeight = 44
    ),
    headlineLarge = googleSansFlexTextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 32,
        lineHeight = 40
    ),
    headlineMedium = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Black,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = googleSansFlexTextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 24,
        lineHeight = 32
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = googleSansFlexTextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14,
        lineHeight = 20
    ),
    bodyLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = googleSansFlexTextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12,
        lineHeight = 16
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Black,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = googleSansFlexTextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 11,
        lineHeight = 16
    )
)
