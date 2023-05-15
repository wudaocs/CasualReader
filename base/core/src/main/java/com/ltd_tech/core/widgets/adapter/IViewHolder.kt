package com.ltd_tech.core.widgets.adapter

import android.view.View
import android.view.ViewGroup

interface IViewHolder<T> {
    fun createItemView(parent: ViewGroup): View?
    fun initView()
    fun onBind(data: T, pos: Int)
    fun onClick()
}