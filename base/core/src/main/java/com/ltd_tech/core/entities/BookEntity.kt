package com.ltd_tech.core.entities

class TxtPage {
    var position = 0
    var title: String? = null
    //当前 lines 中为 title 的行数。
    var titleLines = 0
    var lines: List<String>? = null
}

class TxtChapter {

    //章节所属的小说(网络)
    var bookId: String? = null

    //章节的链接(网络)
    var link: String? = null

    //章节名(共用)
    var title: String? = null

    //章节内容在文章中的起始位置(本地)
    var start: Long = 0

    //章节内容在文章中的终止位置(本地)
    var end: Long = 0

    override fun toString(): String {
        return "TxtChapter{" +
                "title='" + title + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}'
    }
}