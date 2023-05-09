package com.ltd_tech.core.utils

import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 文件操作类
 */

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

@Throws(IOException::class)
fun copyFileUsingFileStreams(source: File, dest: File) {
    var input: InputStream? = null
    var output: OutputStream? = null
    try {
        input = FileInputStream(source)
        output = FileOutputStream(dest)
        val buf = ByteArray(2048)
        var bytesRead: Int
        while (input.read(buf).also { bytesRead = it } > 0) {
            output.write(buf, 0, bytesRead)
        }
    } finally {
        input?.close()
        output?.close()
    }
}

/**
 * 文件类型
 */
enum class CRFileType {
    TXT, EPUB, PDF
}