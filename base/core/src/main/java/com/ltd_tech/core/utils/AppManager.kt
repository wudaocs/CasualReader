package com.ltd_tech.core.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException

var application: Application? = getApplicationBySelf()

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
private fun getApplicationBySelf(): Application? = if (application == null) {
    try {
        val activityThreadClass: Class<*> = Class.forName("android.app.ActivityThread")
        val appField = activityThreadClass.getDeclaredField("mInitialApplication")
        val method = activityThreadClass.getMethod("currentActivityThread", *arrayOfNulls(0))
        val currentAT = method.invoke(null)
        appField.isAccessible = true
        application = appField[currentAT] as Application
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    } catch (e: NoSuchFieldException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    }
    application
} else {
    application
}

// 当期应用的包名
val appPackageName = application?.applicationContext?.packageName ?: "getAppPackageName exception"

// 日志功能是否开启配置
val loggerEnable = getMetaDataByInt("LOG_ENABLE")

// mmkv 公共存储目录
val mmkvSharedDir = getMetaDataByString("mmkv_shared_dir")

/**
 * 获取String类型的meta标签
 */
fun getMetaDataByString(key: String): String = getMeta()?.getString(key) ?: ""

/**
 * 获取Int类型的meta标签
 */
fun getMetaDataByInt(key: String): Int = getMeta()?.getInt(key) ?: 0

/**
 * 获取Boolean类型的meta标签 默认为false
 */
fun getMetaDataByBoolean(key: String): Boolean = getMeta()?.getBoolean(key, false) ?: false

private var weakReference: WeakReference<Bundle>? = null

/**
 * 调整为缓存数据 减少每次调用所需要的耗时
 */
private fun getMeta(): Bundle? {
    return if (weakReference?.get() == null) {
        val meta = application?.let {
            it.packageManager?.getApplicationInfo(it.packageName, PackageManager.GET_META_DATA)
        }
        weakReference = WeakReference<Bundle>(meta?.metaData)
        weakReference?.get()
    } else {
        weakReference?.get()
    }
}