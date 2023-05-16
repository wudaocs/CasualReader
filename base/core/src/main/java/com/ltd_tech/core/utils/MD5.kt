package com.ltd_tech.core.utils

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object MD5 {

    fun MD5(s: String): String? {
        val hexDigits = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F'
        )
        return try {
            val btInput = s.toByteArray()
            // 获得MD5摘要算法的 MessageDigest 对象
            val mdInst = MessageDigest.getInstance("MD5")
            // 使用指定的字节更新摘要
            mdInst.update(btInput)
            // 获得密文
            val md = mdInst.digest()
            // 把密文转换成十六进制的字符串形式
            val j = md.size
            val str = CharArray(j * 2)
            var k = 0
            for (i in 0 until j) {
                val byte0 = md[i]
                str[k++] = hexDigits[byte0.toInt() ushr 4 and 0xf]
                str[k++] = hexDigits[byte0.toInt() and 0xf]
            }
            String(str)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fileGetMD5(file: File?): String? {
        var fis: FileInputStream? = null
        var s: String? = null
        var sTmp: String? = null
        return try {
            val md = MessageDigest.getInstance("MD5")
            fis = FileInputStream(file)
            val buffer = ByteArray(2048)
            var length = -1
            while (fis.read(buffer).also { length = it } != -1) {
                md.update(buffer, 0, length)
            }
            val tmp = md.digest()
            sTmp = getHexString(tmp)
            s = exChange(sTmp)
            s
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            null
        } finally {
            try {
                fis!!.close()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getHexString(bytes: ByteArray): String {
        var result = ""
        for (i in bytes.indices) {
            result += ((bytes[i].toInt() and 0xff) + 0x100).toString(16)
                .substring(1)
        }
        return result
    }

    fun exChange(str: String?): String {
        val sb = StringBuilder()
        if (str != null) {
            for (element in str) {
                if (Character.isUpperCase(element)) {
                    sb.append(element.lowercaseChar())
                } else if (Character.isLowerCase(element)) {
                    sb.append(element.uppercaseChar())
                } else {
                    sb.append(element)
                }
            }
        }
        return sb.toString()
    }

    fun strToMd5By32(str: String): String? {
        var reStr: String? = null
        try {
            val md5 = MessageDigest.getInstance("MD5")
            val bytes = md5.digest(str.toByteArray())
            val stringBuffer = StringBuffer()
            for (b in bytes) {
                val bt = b.toInt() and 0xff
                if (bt < 16) {
                    stringBuffer.append(0)
                }
                stringBuffer.append(Integer.toHexString(bt))
            }
            reStr = stringBuffer.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return reStr
    }

    fun strToMd5By16(str: String): String? {
        var reStr = strToMd5By32(str)
        if (reStr != null) {
            reStr = reStr.substring(8, 24)
        }
        return reStr
    }
}