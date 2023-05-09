package com.ltd_tech.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.util.TypedValue
import android.view.WindowManager

/**
 * author : fzy
 * date   : 2019/11/7 18:36
 */
object ScreenUtils {
    /**
     * 获得屏幕宽度
     */
    val width = application?.resources?.displayMetrics?.widthPixels ?: 0

    /**
     * 获得屏幕高度
     */
    val height = application?.resources?.displayMetrics?.heightPixels ?: 0

    private val density = application?.resources?.displayMetrics?.density ?: 1f

    /**
     *
     * @return 返回全屏宽度
     */
    fun getFullScreenWidth(): Int {
        val wm =
            application?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        val point = Point()
        wm?.defaultDisplay?.getRealSize(point)
        return point.x
    }

    /**
     *
     * @return 返回全屏高度
     */
    fun getFullScreenHeight(): Int {
        val wm =
            application?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        val point = Point()
        wm?.defaultDisplay?.getRealSize(point)
        return point.y
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    @SuppressLint("PrivateApi")
    fun getStatusHeight(context: Context? = null): Int {
        var statusHeight = -1
        try {
            val clazz = Class.forName("com.android.internal.R\$dimen")
            val obj = clazz.newInstance()
            val height = clazz.getField("status_bar_height")[obj]?.toString()?.toInt() ?: 0
            statusHeight = context?.resources?.getDimensionPixelSize(height)
                ?: (application?.resources?.getDimensionPixelSize(height) ?: 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return statusHeight
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     *
     * @param activity
     * @return
     */
    @Suppress("DEPRECATION")
    fun snapShotWithStatusBar(activity: Activity): Bitmap? {
        val view = activity.window.decorView
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        val bmp = view.drawingCache
        val bp: Bitmap? = Bitmap.createBitmap(bmp, 0, 0, width, height)
        view.destroyDrawingCache()
        return bp
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity
     * @return
     */
    @Suppress("DEPRECATION")
    fun snapShotWithoutStatusBar(activity: Activity): Bitmap? {
        val view = activity.window.decorView
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        val bmp = view.drawingCache
        val frame = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(frame)
        val statusBarHeight = frame.top
        val bp: Bitmap? = Bitmap.createBitmap(
            bmp, 0, statusBarHeight, width, height - statusBarHeight
        )
        view.destroyDrawingCache()
        return bp
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dp2px(dpValue: Float): Int {
        return (dpValue * density + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dp(pxValue: Float): Int {
        return (pxValue / density + 0.5f).toInt()
    }

    fun sp2px(sp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp.toFloat(),
            application?.resources?.displayMetrics
        ).toInt()
    }

    /**
     * 是否在屏幕右侧
     *
     * @param xPos     位置的x坐标值
     * @return true：是。
     */
    fun isInRight(xPos: Int): Boolean {
        return xPos > width / 2
    }

    /**
     * 是否在屏幕左侧
     *
     * @param xPos     位置的x坐标值
     * @return true：是。
     */
    fun isInLeft(xPos: Int): Boolean {
        return xPos < width / 2
    }

}

