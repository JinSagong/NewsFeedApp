package com.jin.news

import android.app.Application
import com.jin.news.di.myDiModule
import com.jin.news.util.LanguagePreferences
import org.koin.android.ext.android.startKoin

class MyApplication : Application() {
    companion object {
        lateinit var languagePreferences: LanguagePreferences
    }

    override fun onCreate() {
        super.onCreate()
        startKoin(applicationContext, myDiModule)
        languagePreferences = LanguagePreferences(this)
    }
}