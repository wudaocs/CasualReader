package com.ltd_tech.core.widgets.pager

object ReadConfigManager {

    fun getPageMode(): PageMode {

        // TODO 需要替换为 mmkv方式
        return PageMode.values()[0]
    }

    fun getPageStyle(): PageStyle {

        // TODO 需要替换为 mmkv方式
        return PageStyle.values()[0]
    }

    fun getTextSize(): Int {

        return 20
    }

    fun isNightMode(): Boolean = false

    fun setNightMode(isNightMode: Boolean) {

    }

    fun setPageStyle(pageStyle: PageStyle?) {

    }

    /**
     * 设置当前显示的字体
     */
    fun setTextSize(textSize: Int) {

    }

    fun setPageMode(mode: PageMode?) {

    }

}