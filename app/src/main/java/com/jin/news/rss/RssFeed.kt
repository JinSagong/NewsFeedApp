package com.jin.news.rss

data class RssFeed(var lastBuildDate: String? = null, var items: List<RssItem>? = null)