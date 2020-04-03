package com.jin.news.util

import java.util.*
import kotlin.collections.ArrayList

object KeywordGenerator {
    fun getKeywords(description: String): ArrayList<String?> {
        val result = arrayListOf<String?>()
        val st = StringTokenizer(description, SPECIAL_CHAR_SEQUENCE)
        val map = TreeMap<String, Int>()
        // 단어의 빈도수를 파악함
        while (st.hasMoreTokens()) { st.nextToken()?.let { if (it.length >= 2) map[it] = (map[it] ?: 0) + 1 } }
        // 키워드를 최대 3개까지 생성함
        for (i: Int in 0..2) {
            map.maxBy { it.value }?.let {
                result.add(it.key)
                map.entries.remove(it)
            } ?: result.add(null)
        }
        return result
    }
}