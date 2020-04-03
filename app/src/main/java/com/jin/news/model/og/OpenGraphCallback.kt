package com.jin.news.model.og

import com.jin.news.model.rss.RssItem

interface OpenGraphCallback {
    fun success(item: RssItem)
    fun failure(item: RssItem)
}