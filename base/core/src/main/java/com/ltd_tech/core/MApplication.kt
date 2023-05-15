package com.ltd_tech.core

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.ltd_tech.core.observe.MApplicationObserver
import com.ltd_tech.core.utils.storage.TPS

/**
 * 基类
 */
open class MApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(MApplicationObserver())
        TPS.initMMKV()
    }
}