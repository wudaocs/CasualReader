package com.ltd_tech.core.utils

import android.annotation.SuppressLint
import android.text.format.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 时间处理工具类
 * author: Kaos
 * created on 2023/5/9
 */
object DateUtils {

    const val FORMAT_HH_MM = "HH:mm"

    /** 时间日期格式化到年月日时分秒.  */
    private const val DATE_FORMAT_YMDHMS = "yyyy-MM-dd HH:mm:ss"

    private const val FORMAT_BOOK_DATE = "yyyy-MM-dd'T'HH:mm:ss"

    @SuppressLint("SimpleDateFormat")
    fun getSimpleDateFormat(format: String): SimpleDateFormat {
        val simpleDateFormat = SimpleDateFormat(format)
//        simpleDateFormat.timeZone = TimeZone.getTimeZone(getTimeZone())
        return simpleDateFormat
    }

    //将时间转换成日期
    fun dateConvert(time: Long, pattern: String): String? {
        val date = Date(time)
        val format = getSimpleDateFormat(pattern)
        return format.format(date)
    }

    /**
     * 获取当前系统时间
     */
    fun systemCurrentTime(): Long {
        val simpleDateFormat = getSimpleDateFormat(DATE_FORMAT_YMDHMS)
        return try {
            simpleDateFormat.parse(
                DateFormat.format(DATE_FORMAT_YMDHMS, Date(System.currentTimeMillis()))
                    .toString()
            )?.time ?: 0
        } catch (e: ParseException) {
            e.printStackTrace()
            System.currentTimeMillis()
        }
    }

    /**
     * 获取当前书籍最后阅读时间
     */
    fun getCurrentTimeToBook() =
        dateConvert(System.currentTimeMillis(), FORMAT_BOOK_DATE)
}