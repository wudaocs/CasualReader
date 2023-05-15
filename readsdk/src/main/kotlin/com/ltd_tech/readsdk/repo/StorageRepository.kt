package com.ltd_tech.readsdk.repo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.ltd_tech.core.FILE_SUFFIX_CR
import com.ltd_tech.core.cacheBookDir
import com.ltd_tech.core.cacheDir
import com.ltd_tech.core.utils.L
import com.ltd_tech.core.utils.application
import com.ltd_tech.core.utils.close
import com.ltd_tech.core.utils.deleteFileByPath
import com.ltd_tech.core.utils.getFile
import com.ltd_tech.core.utils.saveToFile
import com.ltd_tech.readsdk.entities.BookChapterTable
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.entities.ChapterInfoEntity
import com.ltd_tech.readsdk.entities.CollectionTable
import com.ltd_tech.readsdk.entities.ReadTable
import com.ltd_tech.readsdk.entities.dao.BookChapterTableDao
import com.ltd_tech.readsdk.entities.dao.CollectionTableDao
import com.ltd_tech.readsdk.entities.dao.DaoMaster
import com.ltd_tech.readsdk.entities.dao.DaoSession
import com.ltd_tech.readsdk.entities.dao.DownloadTaskTableDao
import com.ltd_tech.readsdk.entities.dao.ReadTableDao
import org.greenrobot.greendao.database.Database
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.io.Reader

/**
 * 本地数据操作类
 * author: Kaos
 * created on 2023/5/9
 */
internal class StorageRepository {

    private val tag = "StorageRepository"

    private val daoMaster: DaoManager = DaoManager()

    /**
     * 查询阅读记录
     */
    fun getBookRecord(bookId: String?): ReadTable? {
        return daoMaster.mSession?.readTableDao?.queryBuilder()
            ?.where(ReadTableDao.Properties.BookId.eq(bookId))?.unique()
    }

    /**
     * 保存阅读记录
     */
    fun saveBookRecord(readTable: ReadTable?) {
        daoMaster.mSession?.readTableDao?.insertOrReplace(readTable)
    }

    /**
     * 删除书籍记录
     */
    fun deleteBookRecord(id: String) {
        daoMaster.mSession?.readTableDao?.queryBuilder()
            ?.where(ReadTableDao.Properties.BookId.eq(id))?.buildDelete()
            ?.executeDeleteWithoutDetachingEntities()
    }

    /** ---------↓↓↓---------  章节相关  ---------↓↓↓--------- **/

    /**
     * 保存章节信息
     */
    fun saveChapterInfo(folderName: String, fileName: String, content: String) {
        saveToFile(cacheBookDir + folderName, fileName + FILE_SUFFIX_CR, content)
    }

    /**
     * 获取书籍章节列表 TODO 章节过多时需要增加协程控制
     */
    fun getBookChaptersInRx(bookId: String, done: (MutableList<BookChapterTable>?) -> Unit) {
        done(
            daoMaster.mSession?.bookChapterTableDao?.queryBuilder()
                ?.where(BookChapterTableDao.Properties.BookId.eq(bookId))?.list()
        )
    }
    
    /**
     * 获取章节信息 TODO:需要进行获取编码并转换的问题
     */
    fun getChapterInfo(folderName: String, fileName: String): ChapterInfoEntity? {
        L.il(tag, "获取章节信息相关参数 dir= ${cacheBookDir + folderName}  fileName= $fileName")
        val file = getFile(cacheBookDir + folderName, fileName)
        return if (!file.exists()) null else {
            var reader: Reader? = null
            var str: String?
            val sb = StringBuilder()
            try {
                reader = FileReader(file)
                val br = BufferedReader(reader)
                while (br.readLine().also { str = it } != null) {
                    sb.append(str)
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                close(reader)
            }
            ChapterInfoEntity().apply {
                title = fileName
                body = sb.toString()
            }
        }
    }

    /**
     * 保存书籍章节信息
     */
    fun saveBookChaptersWithAsync(chapters: MutableList<BookChapterTable>) {
        daoMaster.mSession?.run {
            startAsyncSession()?.runInTx {
                bookChapterTableDao.insertOrReplaceInTx(chapters)
            }
        }
    }

    /**
     * 删除书籍章节数据
     */
    fun deleteBookChapter(id: String) {
        daoMaster.mSession?.bookChapterTableDao?.queryBuilder()
            ?.where(BookChapterTableDao.Properties.BookId.eq(id))?.buildDelete()
            ?.executeDeleteWithoutDetachingEntities()
    }

    /** ---------↓↓↓---------  收藏相关  ---------↓↓↓--------- **/

    /**
     * 获取收藏书籍内容
     */
    fun getCollectionTable(bookId: String): CollectionTable? {
        return daoMaster.mSession?.collectionTableDao?.queryBuilder()
            ?.where(CollectionTableDao.Properties._id.eq(bookId))?.unique()
    }

    /**
     * 获取收藏书籍内容
     */
    fun getCollectionTables(): MutableList<CollectionTable> {
        return daoMaster.mSession?.collectionTableDao?.queryBuilder()
            ?.orderDesc(CollectionTableDao.Properties.LastRead)?.list() ?: mutableListOf()
    }

    /**
     * 保存或更新已收藏的书籍 （同步）
     */
    fun insertOrReplaceCollectionTable(collectionTable: CollectionTable) {
        daoMaster.mSession?.collectionTableDao?.insertOrReplace(collectionTable)
    }

    /**
     * 保存或更新已收藏的书籍 （异步）
     */
    fun insertOrReplaceCollectionTables(collectionTable: MutableList<CollectionTable>) {
        daoMaster.mSession?.collectionTableDao?.insertOrReplaceInTx(collectionTable)
    }

    /**
     * 存储已收藏书籍
     */
    fun saveCollectionBookWithAsync(collectionTable: CollectionTable) {
        //启动异步存储
        daoMaster.mSession?.run {
            startAsyncSession()?.runInTx {
                if (!collectionTable.bookChapterList.isNullOrEmpty()) {
                    // 存储bookChapterList
                    bookChapterTableDao.insertOrReplaceInTx(collectionTable.bookChapterList)
                }
                //存储collectionTable (确保先后顺序，否则出错)
                collectionTableDao.insertOrReplace(collectionTable)
            }
        }
    }

    /**
     * 批量存储收藏的书籍
     */
    fun saveCollectionBooksWithAsync(collectionTables: MutableList<CollectionTable>) {
        daoMaster.mSession?.run {
            startAsyncSession()?.runInTx {
                collectionTables.forEach {
                    if (!it.bookChapterList.isNullOrEmpty()) {
                        // TODO 存储bookChapterList(需要修改，如果存在id相同的则无视)
                        bookChapterTableDao.insertOrReplaceInTx(it.bookChapterList)
                    }
                }
                //存储CollectionTable (确保先后顺序，否则出错), 一次性存储 减少性能消耗
                collectionTableDao.insertOrReplaceInTx(collectionTables)
            }
        }
    }


    /**
     * 删除收藏
     */
    fun deleteCollection(collectionTable: CollectionTable) {
        daoMaster.mSession?.collectionTableDao?.delete(collectionTable)
    }

    /**
     * 删除收藏 （异步 包括删除文件） TODO 需要增加协程控制
     */
    fun deleteCollBookInRx(collectionTable: CollectionTable, done: () -> Unit) {
        val bookId = collectionTable._id
        //查看文本中是否存在删除的数据
        deleteFileByPath(cacheBookDir + bookId)
        //删除任务
        deleteDownloadTask(bookId)
        //删除目录
        deleteBookChapter(bookId)
        //删除收藏
        deleteCollection(collectionTable)
        done()
    }

    /** ---------↓↓↓---------  删除下载的任务记录  ---------↓↓↓--------- **/

    /**
     * 删除任务
     */
    fun deleteDownloadTask(bookId: String) {
        daoMaster.mSession?.downloadTaskTableDao?.queryBuilder()
            ?.where(DownloadTaskTableDao.Properties.BookId.eq(bookId))?.buildDelete()
            ?.executeDeleteWithoutDetachingEntities()
    }

    /**
     * 删除缓存文件夹下的所有文件
     */
    fun deleteCache() {
        deleteFileByPath(cacheDir)
    }


    /** ---------↓↓↓---------  书籍相关  ---------↓↓↓--------- **/
    /**
     * 保存书籍信息
     */
    fun saveBookWithAsync(bookEntity: BookEntity?){
        daoMaster.mSession?.run {
            startAsyncSession().runInTx{
                bookEntity?.run {

                    // 如果章节不为空 先存储章节信息
                    if (bookChapterList.isNotEmpty()){
                        bookChapterTableDao.insertOrReplaceInTx(bookChapterList)
                    }
                    // 章节存储完成 在存书籍信息
                    bookEntityDao.insertOrReplace(bookEntity)
                }
            }
        }
    }


}

/**
 * 数据库操作类
 * author: Kaos
 * created on 2023/5/9
 */
open class DaoManager {

    private val DB_NAME = "ltd_read_book"
    private var mDaoMaster: DaoMaster? = null
    private var mDb: SQLiteDatabase? = null
    open var mSession: DaoSession? = null

    init {
        application?.applicationContext?.run {
            //封装数据库的创建、更新、删除
            val openHelper: DaoMaster.DevOpenHelper = DaoHelper(this, DB_NAME)
            mDb = openHelper.writableDatabase
            mDaoMaster = DaoMaster(mDb)
            mSession = mDaoMaster?.newSession()
        }
    }

}

class DaoHelper(context: Context, name: String) : DaoMaster.DevOpenHelper(context, name) {

    override fun onUpgrade(db: Database?, oldVersion: Int, newVersion: Int) {
        super.onUpgrade(db, oldVersion, newVersion)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        super.onDowngrade(db, oldVersion, newVersion)
        // 降级删除所有表
        DaoMaster.dropAllTables(wrap(db), true)
    }

}