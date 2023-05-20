package com.ltd_tech.readsdk.utils

import com.ltd_tech.core.FILE_SUFFIX_CR
import com.ltd_tech.core.cacheBookDir
import com.ltd_tech.core.utils.CRFileType
import com.ltd_tech.core.utils.DateUtils
import com.ltd_tech.core.utils.L
import com.ltd_tech.core.utils.MD5
import com.ltd_tech.readsdk.entities.BookChapterTable
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.entities.ReadTable
import com.ltd_tech.readsdk.repo.StorageRepository
import java.io.File

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
    fun saveBookWithAsync(bookEntity: BookEntity?) {
        mStorageRepository.saveBookWithAsync(bookEntity)
    }

    fun saveBookEntity(bookEntity: BookEntity?){
        mStorageRepository.saveBookEntity(bookEntity)
    }

    /**
     * 获取书籍章节信息
     */
    fun getBookChaptersInRx(bookId: String?, done: (MutableList<BookChapterTable>?) -> Unit) {
        if (bookId == null){
            done(null)
        } else {
            mStorageRepository.getBookChaptersInRx(bookId, done)
        }
    }

    /**
     * 保存章节信息
     */
    fun saveBookChaptersWithAsync(chapters: MutableList<BookChapterTable>){
        mStorageRepository.saveBookChaptersWithAsync(chapters)
    }

    /**
     * 判断是否存在章节缓存文件， 根据文件名判断是否被缓存过 (因为可能数据库显示被缓存过，但是文件中却没有的情况，所以需要根据文件判断是否被缓存
     * 过)
     */
    fun isChapterFileCached(bookId: String?, chapterName: String?): Boolean {
        L.il(
            DataControls::class.java.simpleName,
            "isChapterFileCached -> $cacheBookDir$bookId/$chapterName$FILE_SUFFIX_CR"
        )
        if (bookId == null || chapterName == null) {
            return false
        }
        return File("$cacheBookDir$bookId/$chapterName$FILE_SUFFIX_CR").exists()
    }

    /**
     * 根据本地文件地址保存数据
     */
    fun saveLocal(path : String) : BookEntity{
        val bookEntity = convertBookEntity(mutableListOf(File(path)))
        return bookEntity[0]
    }

    /**
     * 转换本地路径为数据库对象
     */
    private fun convertBookEntity(files: List<File>): List<BookEntity> {
        val bookEntities: MutableList<BookEntity> = ArrayList(files.size)
        for (file in files) {
            //判断文件是否存在
            if (!file.exists()) continue
            val bookEntity = BookEntity()
            bookEntity._id = MD5.strToMd5By16(file.absolutePath)
            if (file.name.endsWith(CRFileType.PDF.value)){
                bookEntity.title = file.name.replace(".pdf", "")
                bookEntity.type = CRFileType.PDF.value
            } else if (file.name.endsWith(CRFileType.EPUB.value)){
                bookEntity.title = file.name.replace(".epub", "")
                bookEntity.type = CRFileType.EPUB.value
            } else {
                bookEntity.title = file.name.replace(".txt", "")
                bookEntity.type = CRFileType.TXT.value
            }
            bookEntity.author = ""
            bookEntity.shortIntro = "无"
            bookEntity.cover = file.absolutePath
            bookEntity.setLocal(true)
            bookEntity.lastChapter = "开始阅读"
            bookEntity.updated = DateUtils.dateConvertByBookDateFormat(
                file.lastModified()
            )
            bookEntity.lastRead = DateUtils.getCurrentTimeToBook()
            bookEntities.add(bookEntity)
        }
        return bookEntities
    }


}