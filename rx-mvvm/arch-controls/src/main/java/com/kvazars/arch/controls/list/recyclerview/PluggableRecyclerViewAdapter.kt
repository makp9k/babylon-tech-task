@file:Suppress("unused")

package com.kvazars.arch.controls.list.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class PluggableRecyclerViewAdapter<T, VH : RecyclerView.ViewHolder>(
    val createViewHolderDelegate: (layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int, adapter: PluggableRecyclerViewAdapter<T, VH>) -> VH,
    val bindViewHolderDelegate: (viewHolder: VH, item: T, position: Int, adapter: PluggableRecyclerViewAdapter<T, VH>) -> Unit,
    areItemsTheSame: (oldItem: T, newItem: T) -> Boolean = { o, n -> o == n },
    areContentsTheSame: (oldItem: T, newItem: T) -> Boolean = { o, n -> o == n }
) : ListAdapter<T, VH>(DiffUtilCallback(areItemsTheSame, areContentsTheSame)) {

    var bindViewHolderWithPayloadsDelegate: ((viewHolder: VH, position: Int, payloads: MutableList<Any>) -> Unit)? = null
    var getItemIdDelegate: ((position: Int) -> Long)? = null
    var getItemViewTypeDelegate: ((Int) -> Int)? = null
    var onAttachedToRecyclerViewDelegate: ((recyclerView: RecyclerView) -> Unit)? = null
    var onDetachedFromRecyclerViewDelegate: ((recyclerView: RecyclerView) -> Unit)? = null
    var onViewAttachedToWindowDelegate: ((holder: VH) -> Unit)? = null
    var onViewDetachedFromWindowDelegate: ((holder: VH) -> Unit)? = null
    var onViewRecycledDelegate: ((holder: VH) -> Unit)? = null
    var onFailedToRecycleViewDelegate: ((holder: VH) -> Boolean)? = null
    var registerAdapterDataObserverDelegate: ((observer: RecyclerView.AdapterDataObserver) -> Unit)? = null
    var unregisterAdapterDataObserverDelegate: ((observer: RecyclerView.AdapterDataObserver) -> Unit)? = null

    private var layoutInflater: LayoutInflater? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }

        return createViewHolderDelegate(layoutInflater!!, parent, viewType, this)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        bindViewHolderDelegate(holder, getItem(position), position, this)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        bindViewHolderWithPayloadsDelegate?.invoke(holder, position, payloads)
            ?: super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemId(position: Int): Long {
        return getItemIdDelegate?.invoke(position)
            ?: super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewTypeDelegate?.invoke(position)
            ?: super.getItemViewType(position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        onAttachedToRecyclerViewDelegate?.invoke(recyclerView)
            ?: super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onViewAttachedToWindow(holder: VH) {
        onViewAttachedToWindowDelegate?.invoke(holder)
            ?: super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        onViewDetachedFromWindowDelegate?.invoke(holder)
            ?: super.onViewDetachedFromWindow(holder)
    }

    override fun onFailedToRecycleView(holder: VH): Boolean {
        return onFailedToRecycleViewDelegate?.invoke(holder)
            ?: return super.onFailedToRecycleView(holder)
    }

    override fun onViewRecycled(holder: VH) {
        onViewRecycledDelegate?.invoke(holder)
            ?: super.onViewRecycled(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        onDetachedFromRecyclerViewDelegate?.invoke(recyclerView)
            ?: super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        registerAdapterDataObserverDelegate?.invoke(observer)
            ?: super.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        unregisterAdapterDataObserverDelegate?.invoke(observer)
            ?: super.unregisterAdapterDataObserver(observer)
    }
}

private class DiffUtilCallback<T>(
    private val areItemsTheSameDelegate: (oldItem: T, newItem: T) -> Boolean,
    private val areContentsTheSameDelegate: (oldItem: T, newItem: T) -> Boolean
) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) = areItemsTheSameDelegate(oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T) = areContentsTheSameDelegate(oldItem, newItem)
}
