package com.ltd_tech.readsdk.page.adapter

import android.view.View
import android.view.ViewGroup
import com.ltd_tech.core.entities.TxtChapter
import com.ltd_tech.core.widgets.adapter.BaseAdapter
import com.ltd_tech.core.widgets.adapter.IViewHolder

class CategoryAdapter : BaseAdapter<TxtChapter?>() {
    private var currentSelected = 0

    override fun onCreateViewHolder(viewType: Int): IViewHolder<TxtChapter?> {
        return CategoryHolder()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view = super.getView(position, convertView, parent)
        val holder = view?.tag as CategoryHolder?
        if (position == currentSelected) {
            holder?.setSelectedChapter()
        }
        return view
    }

    fun setChapter(pos: Int) {
        currentSelected = pos
        notifyDataSetChanged()
    }
}