package com.jin.news.model.repository

import com.jin.news.MyApplication.Companion.languagePreferences
import com.jin.news.model.rss.RssService

class RepositoryImpl(private val service: RssService) : Repository {
    override fun getData(query: String?) = service.getFeed(languagePreferences.getCountryAndLanguage(), query)
}