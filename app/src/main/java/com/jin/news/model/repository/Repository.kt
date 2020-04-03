package com.jin.news.model.repository

import com.jin.news.model.rss.RssFeed
import io.reactivex.Single

interface Repository {
    fun getData(query: String?): Single<RssFeed>
}