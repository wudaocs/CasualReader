package com.ltd_tech.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.ltd_tech.core.R
import com.ltd_tech.core.utils.ScreenUtils.dp2px
import java.lang.reflect.Field

/**
 * 单例Toast工具类
 *
 *
 * 1.解决toast排队的问题
 * 2.修复Toast在android 7.1手机上的BadTokenException
 * 3.兼容位置、时长、stringId
 */
@SuppressLint("DiscouragedPrivateApi")
internal object ToastUtils {
    private var mToast: Toast? = null
    private var mToastLandscape //用于横屏调用的toast，解决变大的问题
            : Toast? = null
    private var mFieldTN: Field? = null
    private var mFieldTNHandler: Field? = null

    init {
        if (Build.VERSION.SDK_INT in 25..28) {
            try {
                mFieldTN = Toast::class.java.getDeclaredField("mTN")
                mFieldTN?.isAccessible = true
                mFieldTNHandler = mFieldTN?.type?.getDeclaredField("mHandler")
                mFieldTNHandler?.isAccessible = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 初始化/获取mToast对象，适配android 7.1，处理BadTokenException
     *
     * @param context Context
     * @return Toast
     */
    @SuppressLint("ShowToast")
    private fun initToast(context: Context): Toast? {
        if (mToast == null) {
            mToast = Toast.makeText(context.applicationContext, "", Toast.LENGTH_SHORT)
            if (Build.VERSION.SDK_INT >= 25) {
                hook(mToast)
            }
        }
        return mToast
    }

    /**
     * 使用自定义布局处理横屏尺寸变大问题
     * 初始化/获取mToast对象，适配android 7.1，处理BadTokenException
     *
     * @param context Context
     * @return Toast
     */
    @SuppressLint("ShowToast")
    private fun initToast(context: Context, meesage: String): Toast? {
        if (mToastLandscape == null) {
            val toastView = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
            val relativeLayout = toastView.findViewById<LinearLayout>(R.id.toast_linear)
            val textView = toastView.findViewById<TextView>(R.id.tv_toast)
            val layoutParams: RelativeLayout.LayoutParams
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    dp2px(28f)
                )
                textView.textSize = 9f
            } else {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    dp2px(45f)
                )
                textView.textSize = 13f
            }
            textView.text = meesage
            relativeLayout.layoutParams = layoutParams
            mToastLandscape = Toast.makeText(context.applicationContext, "", Toast.LENGTH_SHORT)
            mToastLandscape?.view = toastView
            if (Build.VERSION.SDK_INT >= 25) {
                hook(mToastLandscape)
            }
        }
        return mToastLandscape
    }

    /**
     * Toast位置显示在屏幕中间 默认短时长[Toast.LENGTH_SHORT]
     *
     * @param context Context
     * @param content 显示内容
     */
    fun showInCenter(context: Context, content: String?) {
        val toast = initToast(context)
        toast?.setText(content)
        toast?.duration = Toast.LENGTH_SHORT
        toast?.setGravity(Gravity.CENTER, 0, 0)
        toast?.show()
    }

    /**
     * show Toast 可选时长
     *
     * @param context  Context
     * @param message  内容
     * @param duration [Toast.LENGTH_SHORT],[Toast.LENGTH_LONG]
     */
    fun show(context: Context, message: String?, duration: Int = Toast.LENGTH_SHORT) {
        val toast = initToast(context)
        toast?.duration = duration
        toast?.setText(message)
        mToast?.show()
    }
    /**
     * show Toast 可选时长
     *
     * @param context  Context
     * @param stringId 内容id
     * @param duration [Toast.LENGTH_SHORT],[Toast.LENGTH_LONG]
     */
    fun show(context: Context, stringId: Int, duration: Int = Toast.LENGTH_SHORT) {
        val toast = initToast(context)
        toast?.duration = duration
        toast?.setText(stringId)
        mToast?.show()
    }

    /**
     * show Toast 可选位置
     *
     * @param context  Context
     * @param message  内容
     * @param duration [Toast.LENGTH_SHORT],[Toast.LENGTH_LONG]
     */
    fun show(context: Context, message: String?, gravity: Int, duration: Int) {
        val toast = initToast(context)
        toast?.duration = duration
        toast?.setText(message)
        toast?.setGravity(gravity, 0, if (gravity == Gravity.BOTTOM) 50 else 0)
        mToast?.show()
    }
    /**
     * show Toast 横屏界面建议使用，处理变大问题
     *
     * @param context  Context
     * @param message  内容
     * @param duration [Toast.LENGTH_SHORT],[Toast.LENGTH_LONG]
     */
    fun showInLandscape(
        context: Context,
        message: String,
        gravity: Int = Gravity.BOTTOM,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        mToastLandscape = initToast(context, message)
        mToastLandscape?.duration = duration
        mToastLandscape?.setGravity(gravity, 0, if (gravity == Gravity.BOTTOM) 50 else 0)
        mToastLandscape?.show()
    }

    /**
     * 7.1手机上的BadTokenException 相关处理
     *
     * @param toast Toast对象
     */
    private fun hook(toast: Toast?) {
        try {
            val tn = mFieldTN?.get(toast)
            val preHandler = mFieldTNHandler?.get(tn) as Handler?
            mFieldTNHandler!![tn] = FiexHandler(preHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 7.1手机上的BadTokenException 相关处理
     */
    private class FiexHandler(private val impl: Handler?) : Handler() {
        override fun dispatchMessage(msg: Message) {
            try {
                super.dispatchMessage(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun handleMessage(msg: Message) {
            impl?.handleMessage(msg)
        }
    }
}