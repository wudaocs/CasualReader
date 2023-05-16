package com.ltd_tech.readsdk.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.ltd_tech.readsdk.consts.KEY_EXTRA_BOOK
import com.ltd_tech.readsdk.consts.KEY_EXTRA_BOOK_IS_LOCAL
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.page.activities.ReadDetailActivity
import java.io.File

/**
 * 打开书籍详情页面
 */
fun jumpToReadDetail(context: Context, isCollected: Boolean, bookEntity: BookEntity?) {
    context.startActivity(
        Intent(context, ReadDetailActivity::class.java)
            .putExtra(KEY_EXTRA_BOOK_IS_LOCAL, isCollected)
            .putExtra(KEY_EXTRA_BOOK, bookEntity)
    )
}

/**
 * 打开阅读页面, 外部传入的是带有章节下载地址的实体独享
 */
fun jumpToRead(activity: Activity, entity: BookEntity) {
    // 由于默认都是从本地读取 所以直接执行本地打开方法即可
    //id表示本地文件的路径
//    val path: String = entity.collBookBean.cover
//    val file = File(path)
//    //判断这个本地文件是否存在
//    if (file.exists() && file.length() != 0L) {
//        ReadActivity.startActivity(
//            activity,
//            entity.collBookBean, true
//        )
//    } else {
//        // 本地文件不存在 需要下载
//        DM().downloadFiles(object : DownloadListener2() {
//            override fun taskStart(task: DownloadTask) {
//            }
//
//            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
//                ReadActivity.startActivity(
//                    activity,
//                    entity.collBookBean, true
//                )
//            }
//
//        }, DownloadEntity(entity.chapters[0].link, "", ""))
//    }
}