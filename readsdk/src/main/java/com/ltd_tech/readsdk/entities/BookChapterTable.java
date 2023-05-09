package com.ltd_tech.readsdk.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;
@Entity
public class BookChapterTable implements Serializable {

    public static final long serialVersionUID = 536871008;

    /**
     * title : 第一章 他叫白小纯
     * link : http://read.qidian.com/chapter/rJgN8tJ_cVdRGoWu-UQg7Q2/6jr-buLIUJSaGfXRMrUjdw2
     * unreadble : false
     */
    @Id
    private String id;

    private String link;

    private String title;

    //所属的下载任务
    private String taskName;

    private boolean unreadble;

    //所属的书籍
    @Index
    private String bookId;

    //本地书籍参数
    //在书籍文件中的起始位置
    private long start;

    //在书籍文件中的终止位置
    private long end;

    @Generated(hash = 1535794472)
    public BookChapterTable(String id, String link, String title, String taskName,
            boolean unreadble, String bookId, long start, long end) {
        this.id = id;
        this.link = link;
        this.title = title;
        this.taskName = taskName;
        this.unreadble = unreadble;
        this.bookId = bookId;
        this.start = start;
        this.end = end;
    }

    @Generated(hash = 598587959)
    public BookChapterTable() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isUnreadble() {
        return unreadble;
    }

    public void setUnreadble(boolean unreadble) {
        this.unreadble = unreadble;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public boolean getUnreadble() {
        return this.unreadble;
    }
}
