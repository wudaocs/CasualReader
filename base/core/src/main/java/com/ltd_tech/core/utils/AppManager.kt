package com.ltd_tech.core.utils

import android.annotation.SuppressLint
import android.app.Application
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
val appPackageName =  application?.applicationContext?.packageName ?: "getAppPackageName exception"