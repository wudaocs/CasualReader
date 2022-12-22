package com.ltd_tech.core

/**
 * 文件操作类
 */
class F {

    /**
     * 获取txt内容
     */
    fun getTxtContent(from : Long = 0, to : Long = 0) : String {
        // TODO
        return ""
    }

    fun getFileType() : CRFileType {
        return CRFileType.TXT
    }
}

/**
 * 文件类型
 */
enum class CRFileType {
    TXT, EPUB, PDF
}