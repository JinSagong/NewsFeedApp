package com.jin.news.util

import android.content.Context
import android.os.Build
import com.jin.news.MyApplication.Companion.languagePreferences
import java.util.*

object LocaleWrapper {
    fun wrap(base: Context?): Context? {
        val mLocale = Locale(languagePreferences.getLanguage())
        val config = base?.resources?.configuration?.apply {
            // configuration.locale = mLocate 작업이 Deprecated 됨
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) setLocale(mLocale) else locale = mLocale
        } ?: return base
        return base.createConfigurationContext(config)
    }
}