package com.ltd_tech.readsdk.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * BookRecordBean
 */
@Entity
public class ReadTable {
    //所属的书的id
    @Id
    private String bookId;
    //阅读到了第几章
    private int chapter;
    //当前的页码
    private int pagePos;

    @Generated(hash = 1454325058)
    public ReadTable(String bookId, int chapter, int pagePos) {
        this.bookId = bookId;
        this.chapter = chapter;
        this.pagePos = pagePos;
    }

    @Generated(hash = 1623545192)
    public ReadTable() {
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public int getPagePos() {
        return pagePos;
    }

    public void setPagePos(int pagePos) {
        this.pagePos = pagePos;
    }
}
