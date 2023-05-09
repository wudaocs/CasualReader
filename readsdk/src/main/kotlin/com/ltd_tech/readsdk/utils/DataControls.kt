package com.ltd_tech.readsdk.utils

import com.ltd_tech.readsdk.entities.ReadTable
import com.ltd_tech.readsdk.repo.StorageRepository

/**
 * 书籍数据操作类
 * author: Kaos
 * created on 2023/5/9
 */
object DataControls {

    /**
     * 本地操作
     */
    private val mStorageRepository: StorageRepository = StorageRepository()

    /**
     * 根据书籍id查询书籍阅读记录
     */
    fun getBookRecord(bookId: String?): ReadTable {
        return bookId?.run {
            mStorageRepository.getBookRecord(bookId)
        } ?: ReadTable()
    }

    /**
     * 保存阅读记录
     */
    fun saveBookRecord(readTable: ReadTable?) {
        mStorageRepository.saveBookRecord(readTable)
    }

    /**
     * 保存收藏书籍
     */
    fun saveCollBookWithAsync(){

    }


}