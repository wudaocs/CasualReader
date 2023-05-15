package com.ltd_tech.readsdk.page

import android.content.Context
import android.content.Intent
import com.ltd_tech.readsdk.consts.KEY_EXTRA_BOOK
import com.ltd_tech.readsdk.consts.KEY_EXTRA_BOOK_IS_LOCAL
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.page.activities.ReadDetailActivity

/**
 * 打开书籍详情页面
 */
fun jumpToReadDetail(context: Context, isCollected: Boolean, bookEntity: BookEntity?) {
    context.startActivity(
        Intent(context, ReadDetailActivity::class.java)
            .putExtra(KEY_EXTRA_BOOK_IS_LOCAL, isCollected)
            .putExtra(KEY_EXTRA_BOOK, bookEntity)
    )
}