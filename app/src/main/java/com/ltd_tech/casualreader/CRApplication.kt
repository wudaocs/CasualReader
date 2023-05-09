package com.ltd_tech.casualreader

import com.alibaba.android.arouter.launcher.ARouter
import com.ltd_tech.core.MApplication

class CRApplication : MApplication() {

    override fun onCreate() {
        super.onCreate()
        ARouter.init(this)
    }
}