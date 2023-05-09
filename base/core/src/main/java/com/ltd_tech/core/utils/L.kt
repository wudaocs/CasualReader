package com.ltd_tech.core.utils

import android.util.Log
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

/**
 * log printer
 */
object L {
    private val isDebug: Boolean = loggerEnable == 1
    private val TAG = L.javaClass.name

    init {
        Logger.addLogAdapter(AndroidLogAdapter())
    }


    @JvmStatic
    fun v(msg: String? = null) {
        if (isDebug) {
            msg?.run {
                Log.v(TAG, msg)
            }
        }
    }

    @JvmStatic
    fun vl(tag: String = TAG, msg: String? = null) {
        if (isDebug) {
            msg?.run {
                Log.v(tag, msg)
            }
        }
    }

    @JvmStatic
    fun d(msg: Any? = null) {
        if (isDebug) {
            msg?.run {
                Log.d(TAG, msg.toString())
            }
        }
    }

    @JvmStatic
    fun d(msg: String? = null) {
        dl(TAG, msg)
    }

    @JvmStatic
    fun dl(tag: String = TAG, msg: String? = null) {
        if (isDebug) {
            msg?.run {
                Log.d(tag, msg)
            }
        }
    }

    @JvmStatic
    fun e(msg: String? = null) {
        if (isDebug) {
            msg?.run {
                Logger.e(msg)
            }
        }
    }

    @JvmStatic
    fun el(tag: String = TAG, msg: String? = null) {
        if (isDebug) {
            msg?.run {
                Log.e(tag, msg)
            }
        }
    }

    @JvmStatic
    fun i(msg: String? = null) {
        if (isDebug) {
            msg?.run {
                Logger.i(msg)
            }
        }
    }

    @JvmStatic
    fun il(tag: String = TAG, msg: String? = null) {
        if (isDebug) {
            msg?.run {
                Log.i(tag, msg)
            }
        }
    }

    @JvmStatic
    fun w(msg: String? = null) {
        if (isDebug) {
            msg?.run {
                Logger.i(msg)
            }
        }
    }

    @JvmStatic
    fun wl(tag: String = TAG, msg: String? = null) {
        if (isDebug) {
            msg?.run {
                Log.w(tag, msg)
            }
        }
    }

}

