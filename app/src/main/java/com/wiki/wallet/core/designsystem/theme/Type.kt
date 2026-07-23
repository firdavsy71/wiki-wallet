package com.wiki.wallet.core.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wiki.wallet.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val interFont = GoogleFont("Inter")

val InterFontFamily = FontFamily(
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = provider, weight = FontWeight.Bold)
)

object WalletTypography {
    val DisplayXL = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        letterSpacing = (-0.02).em,
        color = WalletColors.TextPrimary
    )

    val DisplayL = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        letterSpacing = (-0.02).em,
        color = WalletColors.TextPrimary
    )

    val TitleM = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        color = WalletColors.TextPrimary
    )

    val BodyM = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = WalletColors.TextMuted
    )

    val LabelM = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = WalletColors.TextMuted
    )

    val LabelS = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        color = WalletColors.TextMuted
    )

    val Mono = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = WalletColors.TextOnDark
    )
}
