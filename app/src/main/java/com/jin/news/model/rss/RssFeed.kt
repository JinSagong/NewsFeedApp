package com.jin.news.model.rss

data class RssFeed(
    var millisCreated: Long? = null,
    var lastBuildDate: String? = null,
    var items: ArrayList<RssItem>? = null
)