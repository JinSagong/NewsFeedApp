package com.jin.news.util

import com.jin.news.R
import splitties.preferences.Preferences

object Preferences : Preferences("default") {
    var languageId by intPref(R.id.languageRadioButton1)
    private val countryAndLanguage = mapOf(
        Pair(R.id.languageRadioButton1, "US:en"),
        Pair(R.id.languageRadioButton2, "KR:ko"),
        Pair(R.id.languageRadioButton3, "JP:ja")
    )

    fun getCountryAndLanguage(): String = countryAndLanguage[languageId] ?: "US:en"

    fun getLanguage(): String = countryAndLanguage[languageId]?.split(":")?.last() ?: "en"
}

