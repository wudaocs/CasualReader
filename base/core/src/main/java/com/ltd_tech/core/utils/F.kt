package com.ltd_tech.core.utils

import android.text.TextUtils
import java.io.BufferedInputStream
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Writer
import java.nio.charset.Charset

/**
 * 文件操作类
 */

const val GSEP = "/"

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

fun deleteFileByPath(path: String) {
    deleteFileByFile(File(path))
}

fun deleteFileByFile(file: File?) {
    if (file == null || !file.exists()) {
        return
    }
    if (file.isFile) {
        readDelete(file)
    } else {
        val files = file.listFiles()
        if (files != null && files.isNotEmpty()) {
            for (f in files) {
                deleteFileByFile(f) // 递归删除每一个文件
            }
        }
        readDelete(file) // 删除该文件夹
    }
}

//解决open failed: EBUSY (Device or resource busy)
private fun readDelete(file: File): Boolean {
    val path = file.parent ?: ""
    return if (!TextUtils.isEmpty(path)) {
        val to = File(path + GSEP + DateUtils.systemCurrentTime())
        file.renameTo(to)
        to.delete()
    } else {
        true
    }
}

/**
 * 存储为文件
 */
fun saveToFile(dirPath: String, filename: String, content: String) {
    //获取流并存储
    var writer: Writer? = null
    try {
        writer = BufferedWriter(FileWriter(getFile(dirPath, filename)))
        writer.write(content)
        writer.flush()
    } catch (e: IOException) {
        e.printStackTrace()
        close(writer)
    }
}

/**
 * 根据路径和文件名获取文件句柄，如果文件不存在则创建文件
 */
fun getFile(dirPath: String, filename: String): File {
    val file = File(dirPath + filename)
    try {
        if (!file.exists()) {
            //创建父类文件夹
            getFolder(file.parent)
            //创建文件
            file.createNewFile()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file
}

/**
 * 获取文件夹，如果文件夹不存在 则创建文件夹
 */
fun getFolder(filePath: String?): File? {
    return filePath?.let {
        val file = File(filePath)
        //如果文件夹不存在，就创建它
        if (!file.exists()) {
            file.mkdirs()
        }
        file
    }
}

/**
 * 文件类型
 */
enum class CRFileType {
    TXT, EPUB, PDF
}

/**
 * 编码类型
 */

enum class CharsetText(val value: String) {
    UTF8("UTF-8"),
    UTF16LE("UTF-16LE"),
    UTF16BE("UTF-16BE"),
    GBK("GBK");

    companion object {
        const val BLANK: Byte = 0x0a
    }
}
//获取文件的编码格式
fun getCharset(fileName: String?): CharsetText {
    var bis: BufferedInputStream? = null
    var charset: CharsetText = CharsetText.GBK
    val first3Bytes = ByteArray(3)
    try {
        var checked = false
        bis = BufferedInputStream(FileInputStream(fileName))
        bis.mark(0)
        var read = bis.read(first3Bytes, 0, 3)
        if (read == -1) return charset
        if (first3Bytes[0] == 0xEF.toByte() && first3Bytes[1] == 0xBB.toByte() && first3Bytes[2] == 0xBF.toByte()) {
            charset = CharsetText.UTF8
            checked = true
        }
        bis.mark(0)
        if (!checked) {
            while (bis.read().also { read = it } != -1) {
                if (read >= 0xF0) break
                if (read in 0x80..0xBF) // 单独出现BF以下的，也算是GBK
                    break
                if (read in 0xC0..0xDF) {
                    read = bis.read()
                    if (read in 0x80..0xBF) // 双字节 (0xC0 - 0xDF)
                    // (0x80 - 0xBF),也可能在GB编码内
                        continue else break
                } else if (read in 0xE0..0xEF) { // 也有可能出错，但是几率较小
                    read = bis.read()
                    if (read in 0x80..0xBF) {
                        read = bis.read()
                        if (read in 0x80..0xBF) {
                            charset = CharsetText.UTF8
                            break
                        } else break
                    } else break
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        close(bis)
    }
    return charset
}