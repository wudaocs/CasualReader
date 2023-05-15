package com.ltd_tech.core.widgets.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class ViewHolderImpl<T> : IViewHolder<T> {

    protected var itemView: View? = null

    protected lateinit var context: Context

    protected abstract val itemLayoutId: Int

    override fun createItemView(parent: ViewGroup): View? {
        itemView = LayoutInflater.from(parent.context).inflate(itemLayoutId, parent, false)
        context = parent.context
        return itemView
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <V : View?> findById(id: Int): V? {
        return itemView?.findViewById<View>(id) as V?
    }

    override fun onClick() {}
}