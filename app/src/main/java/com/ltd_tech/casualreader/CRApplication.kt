package com.ltd_tech.casualreader

import androidx.multidex.MultiDexApplication
import com.alibaba.android.arouter.launcher.ARouter

class CRApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        ARouter.init(this)
    }
}