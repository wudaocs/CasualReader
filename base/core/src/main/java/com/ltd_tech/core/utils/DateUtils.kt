package com.ltd_tech.core.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 时间处理工具类
 */
object DateUtils {

    const val FORMAT_HH_MM = "HH:mm"

    //将时间转换成日期
    @SuppressLint("SimpleDateFormat")
    fun dateConvert(time: Long, pattern: String?): String? {
        val date = Date(time)
        val format = SimpleDateFormat(pattern)
        return format.format(date)
    }
}