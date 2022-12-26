package com.ltd_tech.casualreader

import android.os.Bundle
import com.ltd_tech.core.MBaseActivity

class MainActivity : MBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun bindWidget() {
    }

    override fun toolbar() {
        super.toolbar()
        transparentStatusBar()
    }
}