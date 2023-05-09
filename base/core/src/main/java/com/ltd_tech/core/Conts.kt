package com.ltd_tech.core

import android.os.Environment

// 保持屏幕常亮时间间隔
const val TIME_SCREEN_ON: Long = 90000
const val HANDLER_WHAT_TIME_SCREEN_ON: Int = -1234


var envPath = "${Environment.getExternalStorageDirectory().absolutePath}/cr/"
// 缓存文件夹
var cacheDir = "$envPath/cache/"

// 书籍缓存文件夹
var cacheBookDir = "$cacheDir/bookCache/"
// 阅读记录缓存文件夹
var cacheRecordDir = "$cacheDir/recordCache/"

//采用自己的格式去设置文件，防止文件被系统文件查询到
const val FILE_SUFFIX_CR = ".crr"

const val FILE_SUFFIX_TXT = ".txt"
const val FILE_SUFFIX_EPUB = ".epub"
const val FILE_SUFFIX_PDF = ".pdf"


