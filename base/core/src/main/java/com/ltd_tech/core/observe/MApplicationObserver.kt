package com.ltd_tech.core.observe

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ltd_tech.core.utils.L

/**
 * 检测程序状态  非页面状态
 */
class MApplicationObserver : DefaultLifecycleObserver {

    private val tag = "MApplicationObserver"

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        L.il(tag, "onCreate")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        L.il(tag, "onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        L.il(tag, "onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        L.il(tag, "onStop")
    }

}