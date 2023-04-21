package com.ltd_tech.readsdk.utils

import com.ltd_tech.readsdk.entities.ReadTable
import com.ltd_tech.readsdk.repo.BookRepository
import com.ltd_tech.readsdk.repo.StorageRepository

/**
 * 书籍数据操作类
 */
object DataControls {

    /**
     * 网络操作
     */
    private var mBookRepository: BookRepository = BookRepository()

    /**
     * 本地操作
     */
    private var mStorageRepository: StorageRepository = StorageRepository()

    /**
     * 根据书籍id查询书籍阅读记录
     */
    fun getBookRecord(bookId: String?): ReadTable {
        return bookId?.run {
            StorageRepository().getBookRecord(bookId)
        } ?: ReadTable()
    }


}