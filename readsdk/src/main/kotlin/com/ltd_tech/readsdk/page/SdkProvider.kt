package com.ltd_tech.readsdk.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.ltd_tech.core.exts.globalToast
import com.ltd_tech.core.utils.CRFileType
import com.ltd_tech.core.utils.L
import com.ltd_tech.core.utils.appPackageName
import com.ltd_tech.readsdk.consts.KEY_EXTRA_BOOK
import com.ltd_tech.readsdk.consts.KEY_EXTRA_BOOK_IS_LOCAL
import com.ltd_tech.readsdk.entities.BookEntity
import com.ltd_tech.readsdk.page.activities.ReadDetailActivity
import com.ltd_tech.readsdk.page.activities.ReadPdfOrEpubActivity
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
 * 区分不同文件的类型，打开不同的显示页面
 */
fun jumpToRead(context: Context, bookEntity: BookEntity?) {
    // 判断书籍类型
    when (bookEntity?.type) {
        CRFileType.TXT.value -> {
            context.startActivity(
                Intent(context, ReadDetailActivity::class.java)
                    .putExtra(KEY_EXTRA_BOOK_IS_LOCAL, true)
                    .putExtra(KEY_EXTRA_BOOK, bookEntity)
            )
        }

        CRFileType.PDF.value -> {
            muPdf(context, bookEntity.cover)
        }

        CRFileType.EPUB.value -> {
            val epubFile = File(bookEntity.cover)
            val intent = Intent(context, ReadPdfOrEpubActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", epubFile), null)
            intent.putExtra("$appPackageName.ReturnToLibraryActivity", 1)
            context.startActivity(intent)
        }

        else -> {
            globalToast("暂不支持该文件格式")
        }
    }
}

private fun muPdf(context: Context, path: String) {
    val intent = Intent(context, ReadPdfOrEpubActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    intent.action = Intent.ACTION_VIEW
    intent.setDataAndType(Uri.fromFile(File(path)), null)
    intent.putExtra("$appPackageName.ReturnToLibraryActivity", 1)
    context.startActivity(intent)
}

fun queryEpubFile(context: Context,epubFilePath: String): Cursor? {
    // 定义查询的 Uri 和查询所需的列
    val uri = MediaStore.Files.getContentUri("external")
    val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME)

    // 定义查询条件
    val selection = "${MediaStore.Files.FileColumns.DATA} = ?"
    val selectionArgs = arrayOf(epubFilePath)

    // 执行查询并返回 Cursor
    return context.contentResolver.query(uri, projection, selection, selectionArgs, null)
}

/**
 * 打开阅读页面, 外部传入的是带有章节下载地址的实体独享
 */
fun jumpToRead111(activity: Activity, entity: BookEntity) {
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