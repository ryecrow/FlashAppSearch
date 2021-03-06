package io.github.landerlyoung.flashappsearch.search.repo

import android.util.Log
import android.util.LruCache
import androidx.annotation.WorkerThread
import io.github.landerlyoung.flashappsearch.App
import io.github.landerlyoung.flashappsearch.search.model.PinyinDataBase

/**
 * <pre>
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-23
 * Time:   12:22
 * Life with Passion, Code with Creativity.
 * </pre>
 */
class PinyinConverter {
    companion object {
        private val chineseRegex = "[\\u3400-\\uD87E\\uDDD4]".toRegex(RegexOption.IGNORE_CASE)
        private val nonCharRegex = "([^\\da-zA-Z|>]|\\s)+".toRegex()

        const val PINYIN_SPLITTER_CHAR = '|'
        const val PINYIN_SPLITTER = PINYIN_SPLITTER_CHAR.toString()

        // 多音字
        const val PINYIN_SPLITTER_MULTI_CHAR = '>'
        const val PINYIN_SPLITTER_MULTI = PINYIN_SPLITTER_MULTI_CHAR.toString()

        private val TAG = "PinyinConverter"
    }

    private val db = PinyinDataBase.createDb(App.context)
    private val dao = db.pinyinDao()

    private val pinyinCache = object : LruCache<String, String>(1024) {
        override fun create(key: String?): String? {
            return key?.let {
                dao.queryPinyin(it).foldRight(HashSet<String>()) { pinyin, hs ->
                    hs.add(pinyin)
                    hs
                }.joinToString(separator = PINYIN_SPLITTER_MULTI).let {
                    if (it.isEmpty()) {
                        Log.w(TAG, "can't find pinyin for $key")
                        null
                    } else {
                        it
                    }
                }
            }
        }
    }

    @WorkerThread
    fun hanzi2Pinyin(hanzi: CharSequence): String {
        val pinyin = chineseRegex.replace(hanzi) { mr ->
            val m = mr.value
            " " + (pinyinCache[m] ?: m) + " "
        }
        return nonCharRegex.replace(pinyin, PINYIN_SPLITTER).trim('|')
    }
}