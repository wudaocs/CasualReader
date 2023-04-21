package com.ltd_tech.core.utils

import android.util.DisplayMetrics
import android.util.TypedValue

object ScreenUtil {

    fun dpToPx(dp: Int): Int {
        val metrics: DisplayMetrics = getDisplayMetrics()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
    }

    fun pxToDp(px: Int): Int {
        val metrics: DisplayMetrics = getDisplayMetrics()
        return (px / metrics.density).toInt()
    }

    fun spToPx(sp: Int): Int {
        val metrics: DisplayMetrics = getDisplayMetrics()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), metrics).toInt()
    }

    fun pxToSp(px: Int): Int {
        val metrics: DisplayMetrics = getDisplayMetrics()
        return (px / metrics.scaledDensity).toInt()
    }

    fun getDisplayMetrics(): DisplayMetrics {
        return application?.applicationContext?.resources?.displayMetrics ?: DisplayMetrics()
    }
}