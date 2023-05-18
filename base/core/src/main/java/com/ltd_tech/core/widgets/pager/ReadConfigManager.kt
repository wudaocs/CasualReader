package com.ltd_tech.core.widgets.pager

import com.ltd_tech.core.utils.storage.SdkKV

object ReadConfigManager {

    fun getPageMode(): PageMode {
        return SdkKV.getPageMode()
    }

    fun getPageStyle(): PageStyle {
        return SdkKV.getPageStyle()
    }

    fun getTextSize(): Int {
        return SdkKV.getTextSize()
    }

    fun isNightMode(): Boolean = SdkKV.isNightMode()

    fun setNightMode(isNightMode: Boolean) {
        SdkKV.setNightMode(isNightMode)
    }

    fun setPageStyle(pageStyle: PageStyle?) {
        pageStyle?.run {
            SdkKV.setPageStyle(this)
        }
    }

    /**
     * 设置当前显示的字体
     */
    fun setTextSize(textSize: Int) {
        SdkKV.setTextSize(textSize)
    }

    fun setPageMode(mode: PageMode?) {
        mode?.run {
            SdkKV.setPageMode(this)
        }
    }

}