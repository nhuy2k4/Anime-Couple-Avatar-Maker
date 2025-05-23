package com.brally.mobile.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep

@Keep
enum class LanguageApp(
    val language: String,
    @DrawableRes val flag: Int,
    val languageCode: String,
    var countryCode: String = ""
) {
    ENGLISH(
        language = "English",
        flag = com.braly.ads.R.drawable.flag_uk,
        languageCode = "en"
    ),
    SPANISH(
        language = "Spanish",
        flag = com.braly.ads.R.drawable.flag_es,
        languageCode = "es"
    ),
    VIETNAMESE(
        flag = com.braly.ads.R.drawable.flag_vi, languageCode = "vi", language = "Vietnamese"
    ),
    FRENCH(
        language = "French",
        flag = com.braly.ads.R.drawable.flag_fr,
        languageCode = "fr"
    ),
    INDONESIA(
        language = "Indonesia",
        flag = com.braly.ads.R.drawable.flag_id,
        languageCode = "in"
    ),
    PORTUGUESE(
        language = "Portuguese", flag = com.braly.ads.R.drawable.flag_pt, languageCode = "pt"
    ),
    ROMANIA(
        flag = com.braly.ads.R.drawable.flag_ro,
        languageCode = "ro",
        language = "România"
    ),
    DEUTSCH(
        flag = com.braly.ads.R.drawable.flag_de,
        languageCode = "de",
        language = "Deutsch"
    ),
    ITALIA(
        flag = com.braly.ads.R.drawable.flag_it,
        languageCode = "it",
        language = "Italiano"
    ),
    NETHERLANDS(
        flag = com.braly.ads.R.drawable.flag_ne, languageCode = "nl", language = "Nederland"
    );


    companion object {
        fun getLanguagePosition(languageCode: String): Int {
            val language = entries.find { it.languageCode == languageCode } ?: "en"
            return entries.indexOf(language)
        }
    }
}

@Keep
data class LanguageSelector(
    val language: LanguageApp,
    var isCheck: Boolean = false,

)
