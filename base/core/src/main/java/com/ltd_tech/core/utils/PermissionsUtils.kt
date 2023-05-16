package com.ltd_tech.core.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * 权限申请工具
 * author: Kaos
 * created on 2023/5/16
 */
class PermissionsUtils(context: Context) {
    private val mContext: Context

    init {
        mContext = context.applicationContext
    }

    // 判断权限集合
    fun lacksPermissions(vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (lacksPermission(permission)) {
                return true
            }
        }
        return false
    }

    // 判断是否缺少权限
    private fun lacksPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED
    }
}