package com.ltd_tech.core.widgets.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.CopyOnWriteArrayList

/**
 * rv通用适配器
 * author: Kaos
 * created on 2023/5/14
 */
open class RVBaseAdapter<T, B : ViewDataBinding> (
    private val mContext: Context,
    mDataList: List<T>? = null,
    // 条目布局文件ID
    private val mLayoutId: Int,
    // DataBinding变量ID
    private val mVariableId: Int,
    private val itemClick: ((Int, T) -> Unit)? = null
) : RecyclerView.Adapter<RVBaseHolder<B>>() {

    var mAllList = CopyOnWriteArrayList<T>()

    init {
        if (!mDataList.isNullOrEmpty()) {
            mAllList.addAll(mDataList)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(mDataList: List<T>?) {
        mDataList?.run {
            mAllList = CopyOnWriteArrayList(this)
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeData(t: T) {
        mAllList.remove(t)
        notifyDataSetChanged()
    }

    /**
     * 添加  单个 或 多个 item使用，
     * 添加完再进行刷新
     * 使用可变参数，不用外界进行新建list进行添加
     */
    fun addMoreItem(vararg data: T) {
        data.let {
            val size = mAllList.size
            mAllList.addAll(it)
            notifyItemInserted(size)
        }
    }

    /**
     * 添加list时候使用
     */
    @SuppressLint("NotifyDataSetChanged")
    fun addMoreData(dataList: List<T>) {
        if (dataList.isNotEmpty()) {
            mAllList.addAll(dataList)
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshItems(dataList: List<T>?){
        dataList?.run {
            mAllList.clear()
            mAllList.addAll(dataList)
            notifyDataSetChanged()
        }
    }

    var indexTemp = 0

    @Suppress("UNCHECKED_CAST")
    open fun getItem(index: Int): T {
        indexTemp = index
        if (indexTemp >= mAllList.size) {
            indexTemp = mAllList.size - 1
        }
        return mAllList[indexTemp]
    }

    fun getData(): CopyOnWriteArrayList<T> {
        return mAllList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RVBaseHolder<B> {
        val binding: B =
            DataBindingUtil.inflate(LayoutInflater.from(mContext), mLayoutId, parent, false)
        return RVBaseHolder<B>(binding.root).apply {
            this.binding = binding
        }
    }

    override fun getItemCount(): Int {
        return mAllList.size
    }

    override fun onBindViewHolder(holder: RVBaseHolder<B>, position: Int) {
        holder.binding.setVariable(mVariableId, getItem(position))
        // 别忘记这句代码
        holder.binding.executePendingBindings()
        // item点击
        holder.itemView.tag = position
        if (itemClick != null) {
            holder.itemView.setOnClickListener {
                val pos = it.tag as Int
                //修复特殊情况：索引越界的问题
                if (pos < mAllList.size) {
                    itemClick.invoke(pos, getItem(pos))
                }
            }
        }
        bindView(holder.binding, getItem(position), position)
    }

    open fun bindView(binding: B, t: T, position: Int) {

    }

}

class RVBaseHolder<B : ViewDataBinding>(view: View) : RecyclerView.ViewHolder(view){
    lateinit var binding: B
}