package com.ltd_tech.core.utils

import java.io.Closeable
import java.io.IOException

/**
 * 文件操作类
 */
class F {

    /**
     * 获取txt内容
     */
    fun getTxtContent(from: Long = 0, to: Long = 0): String {
        // TODO
        return ""
    }

    fun getFileType(): CRFileType {
        return CRFileType.TXT
    }

    /**
     * 关闭文件流
     */
    fun close(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

/**
 * 文件类型
 */
enum class CRFileType {
    TXT, EPUB, PDF
}