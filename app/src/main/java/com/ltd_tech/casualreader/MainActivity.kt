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
import com.ltd_tech.readsdk.page.jumpToReadDetail
import com.ltd_tech.readsdk.utils.DataControls
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

        bind.btnRead.setOnClickListener {
            jumpToReadDetail(
                this@MainActivity,
                true,
                DataControls.saveLocal("${Environment.getExternalStorageDirectory().absolutePath}/Download/中外名人成功故事.txt")
            )
        }

    }


    /**
     * 需要在有权限之后处理文件
     */
    private fun dealFile() {
        val file =
            File("${Environment.getExternalStorageDirectory().absolutePath}/Download/中外名人成功故事.txt")
        if (!file.exists()) {
            file.createNewFile()
        } else {
            // 如果存在先删除 保证每次的文件内容都是最新的
            file.delete()
        }

        // 文件不存在 拷贝文件到该目录
        copyFile(file)
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
                dealFile()
            }
        } else {
            isPermission = true
            dealFile()
        }
    }

    private fun copyFile(dest: File) {
        assets.open("中外名人成功故事.txt").use { fis ->
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
                    dealFile()
                } else {
                    isPermission = false
                    toast("用户拒绝开启读写权限")
                }
                return
            }
        }
    }

}