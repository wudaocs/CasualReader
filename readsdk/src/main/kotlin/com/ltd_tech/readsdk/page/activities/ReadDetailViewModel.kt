package com.ltd_tech.readsdk.page.activities

import com.ltd_tech.core.BaseViewModel
import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.readsdk.entities.BookChapterTable

/**
 * 书籍详情页面viewModel
 * author: Kaos
 * created on 2023/5/14
 */
class ReadDetailViewModel : BaseViewModel() {

    fun loadCategory(bookId: String?, refreshUi: (MutableList<BookChapterTable>?) -> Unit) {

        // 网络请求回来的章节信息显示在页面上
        refreshUi(null)
    }

    /**
     * 加载章节信息
     */
    fun loadChapter(
        bookId: String?,
        bookChapters: MutableList<TxtChapter>,
        refreshUi: (Boolean) -> Unit
    ) {

    }


}