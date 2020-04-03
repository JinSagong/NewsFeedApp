package com.jin.news.util

import android.content.Context
import com.jin.news.R

class LanguagePreferences(ctx: Context) {
    private val pref = ctx.getSharedPreferences("language", Context.MODE_PRIVATE)

    var languageId: Int
        get() = pref.getInt("__key__", R.id.languageRadioButton1)
        set(value) = pref.edit().putInt("__key__", value).apply()

    private val countryAndLanguage = mapOf(
        Pair(R.id.languageRadioButton1, "US:en"),
        Pair(R.id.languageRadioButton2, "KR:ko"),
        Pair(R.id.languageRadioButton3, "JP:ja")
    )

    fun getCountryAndLanguage(): String = countryAndLanguage[languageId] ?: "US:en"

    fun getLanguage(): String = countryAndLanguage[languageId]?.split(":")?.last() ?: "en"
}