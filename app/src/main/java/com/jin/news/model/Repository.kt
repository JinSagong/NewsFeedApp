package com.jin.news.model

import com.jin.news.rss.RssFeed
import io.reactivex.Single

interface Repository {
    fun getData(countryAndLanguage: String?, query: String?): Single<RssFeed>
}