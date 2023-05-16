package com.ltd_tech.readsdk.page.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.core.widgets.adapter.RVBaseAdapter
import com.ltd_tech.core.widgets.pager.PageStyle
import com.ltd_tech.readsdk.BR
import com.ltd_tech.readsdk.R
import com.ltd_tech.readsdk.databinding.ItemCategoryBinding
import com.ltd_tech.readsdk.databinding.ItemSettingReadBgBinding
import com.ltd_tech.readsdk.utils.DataControls

/**
 * 设置弹框设置背景色适配器
 * author: Kaos
 * created on 2023/5/14
 */
class SettingBackgroundAdapter(
    context: Context,
    dataList: MutableList<PageStyle>,
    onItemClick: (Int, PageStyle) -> Unit
) :
    RVBaseAdapter<PageStyle, ItemSettingReadBgBinding>(
        context,
        dataList,
        R.layout.item_setting_read_bg,
        BR.ItemSettingBackground,
        onItemClick
    ) {

    private var mCurrentPageStyle: PageStyle = PageStyle.BG_0

    override fun bindView(binding: ItemSettingReadBgBinding, t: PageStyle, position: Int) {
        super.bindView(binding, t, position)

        binding.run {
            bgItemSettingReadBgView.setBackgroundResource(t.bgColor)
            if (t == mCurrentPageStyle){
                ivItemSettingReadBgChecked.visibility = View.VISIBLE
            } else {
                ivItemSettingReadBgChecked.visibility = View.GONE
            }
        }
    }

    /**
     * 设置当前样式
     */
    fun setPageStyle(pageStyle: PageStyle){
        mCurrentPageStyle = pageStyle
        notifyDataSetChanged()
    }

}