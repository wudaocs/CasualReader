package com.ltd_tech.core.exts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ltd_tech.core.utils.ResourceUtils
import com.ltd_tech.core.utils.ToastUtils
import com.ltd_tech.core.utils.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


fun Activity?.toast(message: Int) {
    this?.toast(ResourceUtils.getString(message))
}

@JvmOverloads
fun Activity?.toast(message: String?, gravity: Int = Gravity.BOTTOM) {
    this?.let {
        showToast(it, message, gravity)
    }
}


fun Fragment?.toast(message: Int) {
    this?.toast(ResourceUtils.getString(message))
}

@JvmOverloads
fun Fragment?.toast(message: String?, gravity: Int = Gravity.BOTTOM) {
    this?.let {
        showToast(it.context, message, gravity)
    }
}

@JvmOverloads
fun Context?.toast(message: Int, gravity: Int = Gravity.BOTTOM) {
    this?.toast(ResourceUtils.getString(message), gravity)
}

@JvmOverloads
fun Context?.toast(message: String?, gravity: Int = Gravity.BOTTOM) {
    this?.let {
        showToast(it, message, gravity)
    }
}

@SuppressLint("ShowToast")
@JvmOverloads
private fun showToast(context: Context?, message: String?, showGravity: Int = Gravity.BOTTOM) {
    message?.run {
        GlobalScope.launch(Dispatchers.Main) {
            context?.run {
                ToastUtils.show(this, message, showGravity, Toast.LENGTH_SHORT)
            }
        }
    }
}

/**
 * 全局弹出toast
 */
@JvmOverloads
fun globalToast(messageId: Int, gravity: Int = Gravity.BOTTOM) {
    application?.run {
        showToast(applicationContext, ResourceUtils.getString(messageId), gravity)
    }
}

/***
 * 适用于吐司信息由服务端返回的情况
 */
@JvmOverloads
fun globalToast(message: String, gravity: Int = Gravity.BOTTOM) {
    application?.run {
        showToast(applicationContext, message, gravity)
    }
}

