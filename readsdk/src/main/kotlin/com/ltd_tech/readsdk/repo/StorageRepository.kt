package com.ltd_tech.readsdk.repo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.ltd_tech.core.utils.application
import com.ltd_tech.readsdk.entities.ReadTable
import com.ltd_tech.readsdk.entities.dao.DaoMaster
import com.ltd_tech.readsdk.entities.dao.DaoSession
import com.ltd_tech.readsdk.entities.dao.ReadTableDao
import org.greenrobot.greendao.database.Database

/**
 * 本地数据操作类
 */
internal class StorageRepository {

    private val daoMaster: DaoManager = DaoManager()

    fun getBookRecord(bookId: String?) : ReadTable? {
        return daoMaster.mSession?.readTableDao?.queryBuilder()
            ?.where(ReadTableDao.Properties.BookId.eq(bookId))?.unique()
    }
}

/**
 * 数据库操作类
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