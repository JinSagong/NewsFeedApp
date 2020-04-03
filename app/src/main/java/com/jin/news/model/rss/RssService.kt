package com.jin.news.model.rss

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RssService {
    @GET("rss")
    fun getFeed(
        @Query("ceid") countryAndLanguage: String?,
        @Query("q") query: String?
    ): Single<RssFeed>
}