package com.jin.news.rss

import android.util.Log
import com.jin.news.util.TimeFormat
import okhttp3.ResponseBody
import org.xml.sax.InputSource
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.xml.parsers.SAXParserFactory

class RssConverterFactory : Converter.Factory() {
    companion object {
        fun create() = RssConverterFactory()
    }

    override fun responseBodyConverter(
        type: Type, annotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<ResponseBody, *>? = Converter<ResponseBody, RssFeed> { value ->
        RssFeed().apply {
            try {
                val parser = RssParser()
                val xmlReader = SAXParserFactory.newInstance().newSAXParser().xmlReader
                xmlReader.contentHandler = parser
                xmlReader.parse(InputSource(value.charStream()))
                lastBuildDate = TimeFormat.getTime(parser.lastBuildDate)
                items = parser.items
                Log.d(
                    this.javaClass.simpleName,
                    "[Conversion Success] Last Build Date: ${lastBuildDate ?: ""}" +
                            ", Number of Feeds: ${items?.size ?: 0}"
                )
            } catch (e: Exception) {
                Log.d(this.javaClass.simpleName, "[Conversion Failure] ${e.printStackTrace()}")
            }
        }
    }
}