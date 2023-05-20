package com.ltd_tech.casualreader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.app.ActivityCompat
import com.ltd_tech.casualreader.databinding.ActivityMainBinding
import com.ltd_tech.core.MBaseActivity
import com.ltd_tech.core.exts.toast
import com.ltd_tech.core.utils.PermissionsUtils
import com.ltd_tech.readsdk.page.activities.ReadDetailViewModel
import com.ltd_tech.readsdk.page.jumpToRead
import com.ltd_tech.readsdk.utils.DataControls
import kotlinx.coroutines.GlobalScope
import java.io.File
import java.io.FileOutputStream

class MainActivity : MBaseActivity<ActivityMainBinding, ReadDetailViewModel>() {

    private lateinit var mPermissionsUtils: PermissionsUtils

    private val PERMISSIONS_REQUEST_STORAGE = 1777

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var isPermission = false
    override fun layout(): Any = R.layout.activity_main

    override fun bindWidget() {
        mPermissionsUtils = PermissionsUtils(this)
        checkPermissions()

        bind.btnReadTxt.setOnClickListener {

            jumpToRead(this@MainActivity,
                DataControls.saveLocal("${Environment.getExternalStorageDirectory().absolutePath}/Download/中外名人成功故事.txt"))

//            jumpToReadDetail(
//                this@MainActivity,
//                true,
//                DataControls.saveLocal("${Environment.getExternalStorageDirectory().absolutePath}/Download/中外名人成功故事.txt")
//            )
        }

        bind.btnReadPdf.setOnClickListener {
            jumpToRead(this@MainActivity,
                DataControls.saveLocal("${Environment.getExternalStorageDirectory().absolutePath}/Download/Jetpack Compose 入门到精通.pdf"))
        }

        bind.btnReadEpub.setOnClickListener {
            jumpToRead(this@MainActivity,
                DataControls.saveLocal("${Environment.getExternalStorageDirectory().absolutePath}/Download/作战篇.epub"))
        }

    }

    /**
     * 同步文件操作
     */
    private fun syncFile(){
        dealFile("中外名人成功故事.txt")
        dealFile("Jetpack Compose 入门到精通.pdf")
        dealFile("作战篇.epub")
    }

    /**
     * 需要在有权限之后处理文件
     */
    private fun dealFile(fileName : String) {
        val file =
            File("${Environment.getExternalStorageDirectory().absolutePath}/Download/$fileName")
        if (!file.exists()) {
            file.createNewFile()
        } else {
            // 如果存在先删除 保证每次的文件内容都是最新的
            file.delete()
        }

        // 文件不存在 拷贝文件到该目录
        copyFile(fileName,file)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //获取读取和写入SD卡的权限
            if (mPermissionsUtils.lacksPermissions(*PERMISSIONS)) {
                isPermission = false
                //请求权限
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    PERMISSIONS_REQUEST_STORAGE
                )
            } else {
                isPermission = true
                syncFile()
            }
        } else {
            isPermission = true
            syncFile()
        }
    }

    private fun copyFile(fileName: String, dest: File) {
        assets.open(fileName).use { fis ->
            FileOutputStream(dest).use { os ->
                val buffer = ByteArray(1024)
                var len: Int
                while (fis.read(buffer).also { len = it } != -1) {
                    os.write(buffer, 0, len)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_STORAGE -> {
                // 如果取消权限，则返回的值为0
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    isPermission = true
                    syncFile()
                } else {
                    isPermission = false
                    toast("用户拒绝开启读写权限")
                }
                return
            }
        }
    }

}