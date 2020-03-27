package com.jin.news.util

import android.content.Context
import android.os.Build
import java.util.*

class LocaleWrapper {
    companion object {
        fun wrap(base: Context?): Context? {
            val mLocale = Locale(Preferences.getLanguage())
            val config = base?.resources?.configuration?.apply {
                @Suppress("DEPRECATION")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) setLocale(mLocale) else locale = mLocale
            } ?: return base
            return base.createConfigurationContext(config)
        }
    }
}