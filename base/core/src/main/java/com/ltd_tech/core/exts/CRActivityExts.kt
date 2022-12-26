package com.ltd_tech.core.exts

import android.os.Build
import android.text.TextUtils
import androidx.annotation.ColorRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.ltd_tech.core.MBaseActivity

/**
 * WindowInsetsCompat.Type.statusBars()
 * WindowInsetsCompat.Type.navigationBars()
 * WindowInsetsCompat.Type.captionBar()
 * WindowInsetsCompat.Type.ime()
 * WindowInsetsCompat.Type.systemGestures()
 * WindowInsetsCompat.Type.mandatorySystemGestures()
 * WindowInsetsCompat.Type.tappableElement()
 * WindowInsetsCompat.Type.displayCutout()
 * WindowInsetsCompat.Type.systemBars()
 */
fun MBaseActivity.setControllerFlag(
    flag: Int,
    show: Boolean = true,
    reverseColor: Boolean = false
) {
    WindowCompat.getInsetsController(window, window.decorView).run {
        if (flag == WindowInsetsCompat.Type.systemBars()) {
            // show -> true 设置状态栏反色 ,false 取消状态栏反色
            isAppearanceLightStatusBars = reverseColor
            if (show) {
                // 同时显示状态栏和导航栏
                show(flag)
            } else {
                // 同时隐藏状态栏和导航栏
                hide(flag)
            }
        }
        if (flag == WindowInsetsCompat.Type.navigationBars()) {
            // show -> true 设置导航栏反色, false 取消导航栏反色
            isAppearanceLightNavigationBars = reverseColor
        }
        // 隐藏状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (flag == WindowInsetsCompat.Type.statusBars()) {
                if (show) {
                    // 显示状态栏
                    show(flag)
                } else {
                    // 隐藏状态栏
                    hide(flag)
                }
            }

            if (flag == WindowInsetsCompat.Type.navigationBars()) {
                if (show) {
                    // 显示导航栏
                    show(flag)
                } else {
                    // 隐藏导航栏
                    hide(flag)
                }
            }
        }
    }
}

/**
 * 设置状态栏颜色
 */
fun MBaseActivity.setStatusBarColor(@ColorRes colorRes: Int = -1, colorResName: String = "") {
    window.statusBarColor = if (colorRes != -1) {
        resources.getColor(colorRes)
    } else {
        if (TextUtils.isEmpty(colorResName)) {
            resources.getColor(android.R.color.transparent)
        } else {
            resources.getColor(colorRes)
        }

    }
}