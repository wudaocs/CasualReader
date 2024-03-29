package com.ltd_tech.readsdk.loader

import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.readsdk.views.PagerView
import com.ltd_tech.readsdk.entities.BookEntity
import java.io.BufferedReader

class NetPageLoader(pagerView: PagerView, bookEntity: BookEntity): BookLoader(pagerView,bookEntity) {
    override fun refreshChapterList() {
    }

    override fun getChapterReader(chapter: TxtChapter?): BufferedReader? {
        return null
    }

    override fun hasChapterData(chapter: TxtChapter?): Boolean {
        return false
    }
}