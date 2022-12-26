package com.ltd_tech.core

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.ltd_tech.core.exts.setControllerFlag
import com.ltd_tech.core.exts.setStatusBarColor
import com.ltd_tech.core.provider.ILoginProvider

/**
 * 项目基类
 */
abstract class MBaseActivity : BaseActivity(){

    // 标示当前页面状态, 用于处理程序异常再次打开页面时引起的连续崩溃问题
    private var status = 0

    // 基类中提供的handler操作
    protected val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                HANDLER_WHAT_TIME_SCREEN_ON -> {
                    // 处理屏幕常亮时间问题
                    sendKeepOnMessage(msg)
                }
                else -> {
                    handlerMessage(msg)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkActivity(savedInstanceState)
        lifecycleScope.launchWhenCreated {
            bindWidget()
        }
    }

    private fun checkActivity(savedInstanceState: Bundle?){
        // 如果产生异常则调用退出
        if (savedInstanceState != null && status == 0) {
            // 如果异常踢回到登录
            ARouter.getInstance().navigation(ILoginProvider::class.java)?.logout()
        }
        status = 1
    }

    /**
     * 加载传入参数
     */
    @Suppress("unused")
    open fun initArgus(){}
    /**
     * 绑定视图
     */
    abstract fun bindWidget()

    open fun toolbar(){}

    /**
     * 是否保持当前页面屏幕常亮
     */
    open fun isKeepScreenOn() = false

    /**
     * 半透明化StatusBar
     */
    protected fun transparentStatusBar(){
        setControllerFlag(WindowInsetsCompat.Type.statusBars())
        setStatusBarColor()
    }


    /**
     * 开启亮屏
     */
    @Suppress("unused")
    private fun keepScreenOn() {
        if (isKeepScreenOn()) {
            mHandler.sendMessage(Message.obtain().apply {
                what = HANDLER_WHAT_TIME_SCREEN_ON
                obj = true
            })
        }
    }


    /**
     * 设置当前页面屏幕常亮时间延迟时间
     */
    open fun keepScreenOnSpaceTime() = TIME_SCREEN_ON

    /**
     * 基类提供的handler回调方法
     */
    open fun handlerMessage(msg: Message) {

    }

    /**
     * 设置屏幕亮与灭
     */
    private fun keepScreenOnOrOff(isKeepOn: Boolean) {
        if (isKeepOn) {
            // 亮
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            // 灭
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }


    /**
     * 处理亮屏灭屏消息
     */
    private fun sendKeepOnMessage(msg: Message) {
        if (isKeepScreenOn()) {
            // 如果开启控制则先打开 在延迟关闭
            if (msg.obj == true) {
                keepScreenOnOrOff(true)
            } else {
                keepScreenOnOrOff(false)
            }
            // 延迟关闭
            mHandler.sendMessageDelayed(Message.obtain().apply {
                what = HANDLER_WHAT_TIME_SCREEN_ON
                obj = false
            }, keepScreenOnSpaceTime())
        }
    }

}