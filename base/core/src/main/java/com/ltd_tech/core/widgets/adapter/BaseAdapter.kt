package com.ltd_tech.core.widgets.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * 一般类型适配器
 * author: Kaos
 * created on 2023/5/14
 */
abstract class BaseAdapter<T> : BaseAdapter() {

    private val mList: MutableList<T> = ArrayList()

    override fun getCount() = mList.size

    override fun getItem(position: Int): T = mList[position]

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun addItem(value: T) {
        mList.add(value)
        notifyDataSetChanged()
    }

    fun addItem(index: Int, value: T) {
        mList.add(index, value)
        notifyDataSetChanged()
    }

    fun addItems(values: List<T>?) {
        mList.addAll(values!!)
        notifyDataSetChanged()
    }

    fun removeItem(value: T) {
        mList.remove(value)
        notifyDataSetChanged()
    }

    fun refreshItems(list: List<T>?) {
        mList.clear()
        list?.run {
            mList.addAll(this)
        }
        notifyDataSetChanged()
    }

    fun clear() {
        mList.clear()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var cv = convertView
        val holder: IViewHolder<T>
        if (cv == null) {
            holder = onCreateViewHolder(getItemViewType(position))
            cv = holder.createItemView(parent)
            cv?.tag = holder
            //初始化
            holder.initView()
        } else {
            holder = cv.tag as IViewHolder<T>
        }
        val item = getItem(position)
        //执行绑定
        holder.onBind(item, position)
        return cv
    }

    protected abstract fun onCreateViewHolder(viewType: Int): IViewHolder<T>
}