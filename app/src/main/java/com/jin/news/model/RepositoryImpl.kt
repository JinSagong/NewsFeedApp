package com.jin.news.model

import com.jin.news.rss.RssService

class RepositoryImpl(private val service: RssService) : Repository {
    override fun getData(countryAndLanguage: String?, query: String?) =
        service.getFeed(countryAndLanguage, query)
}