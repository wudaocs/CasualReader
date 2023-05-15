package com.ltd_tech.readsdk.page.adapter

import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.core.widgets.adapter.ViewHolderImpl
import com.ltd_tech.readsdk.R
import com.ltd_tech.readsdk.utils.DataControls.isChapterFileCached

/**
 * 章节目录、
 * author: Kaos
 * created on 2023/5/14
 */
class CategoryHolder : ViewHolderImpl<TxtChapter?>() {

    private var mTvChapter: TextView? = null
    override fun initView() {
        mTvChapter = findById<TextView>(R.id.category_tv_chapter)
    }

    override fun onBind(data: TxtChapter?, pos: Int) {
        //首先判断是否该章已下载
        //TODO:目录显示设计的有点不好，需要靠成员变量是否为null来判断。
        //如果没有链接地址表示是本地文件
        val drawable: Drawable? = if (data?.link == null) {
            ContextCompat.getDrawable(context, R.drawable.selector_category_load)
        } else {
            ContextCompat.getDrawable(
                context,
                if (isChapterFileCached(data.bookId ?: "", data.title ?: "")) {
                    R.drawable.selector_category_load
                } else {
                    R.drawable.selector_category_unload
                }
            )
        }
        mTvChapter?.isSelected = false
        mTvChapter?.setTextColor(
            ContextCompat.getColor(
                context,
                com.ltd_tech.core.R.color.text_default
            )
        )
        mTvChapter?.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        mTvChapter?.text = data?.title
    }

    override val itemLayoutId: Int = R.layout.item_category

    fun setSelectedChapter() {
        mTvChapter?.setTextColor(
            ContextCompat.getColor(
                context,
                com.ltd_tech.core.R.color.light_red
            )
        )
        mTvChapter?.isSelected = true
    }
}