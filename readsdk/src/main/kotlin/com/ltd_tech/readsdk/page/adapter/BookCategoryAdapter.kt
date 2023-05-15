package com.ltd_tech.readsdk.page.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.core.widgets.adapter.RVBaseAdapter
import com.ltd_tech.readsdk.BR
import com.ltd_tech.readsdk.R
import com.ltd_tech.readsdk.databinding.ItemCategoryBinding
import com.ltd_tech.readsdk.utils.DataControls

/**
 * 书籍目录适配器
 * author: Kaos
 * created on 2023/5/14
 */
class BookCategoryAdapter(
    context: Context,
    dataList: MutableList<TxtChapter>,
    onItemClick: (Int, TxtChapter) -> Unit
) :
    RVBaseAdapter<TxtChapter, ItemCategoryBinding>(
        context,
        dataList,
        R.layout.item_category,
        BR.ItemCategoryTxtChapter,
        onItemClick
    ) {
    // 当前选中的条目
    private var currentSelected = 0

    override fun bindView(binding: ItemCategoryBinding, t: TxtChapter, position: Int) {
        super.bindView(binding, t, position)
        binding.categoryTvChapter.run {
            //首先判断是否该章已下载
            //TODO:目录显示设计的有点不好，需要靠成员变量是否为null来判断。
            //如果没有链接地址表示是本地文件
            val drawable: Drawable? = if (t.link == null) {
                ContextCompat.getDrawable(context, R.drawable.selector_category_load)
            } else {
                if (DataControls.isChapterFileCached(t.bookId, t.title)) {
                    ContextCompat.getDrawable(context, R.drawable.selector_category_load)
                } else {
                    ContextCompat.getDrawable(context, R.drawable.selector_category_unload)
                }
            }

            if (position == currentSelected) {
                isSelected = true
                setTextColor(ContextCompat.getColor(context, com.ltd_tech.core.R.color.light_red))
            } else {
                isSelected = false
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        com.ltd_tech.core.R.color.text_default
                    )
                )
            }
            setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

    }

    /**
     * 设置当前选中的章节
     */
    fun setCurrentChapter(position: Int?) {
        if (position != null) {
            val temp = currentSelected
            currentSelected = position
            notifyItemChanged(temp)
            notifyItemChanged(position)
        }
    }
}