package com.ltd_tech.casualreader

import com.ltd_tech.casualreader.databinding.ActivityMainBinding
import com.ltd_tech.core.BaseViewModel
import com.ltd_tech.core.MBaseActivity
import com.ltd_tech.readsdk.page.activities.ReadDetailViewModel

class MainActivity : MBaseActivity<ActivityMainBinding, ReadDetailViewModel>() {

    override fun layout(): Any = R.layout.activity_main

    override fun bindWidget() {
    }

}