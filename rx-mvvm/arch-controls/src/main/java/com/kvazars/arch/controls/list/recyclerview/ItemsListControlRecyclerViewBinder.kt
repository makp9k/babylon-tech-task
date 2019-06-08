@file:Suppress("unused")

package com.kvazars.arch.controls.list.recyclerview

import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kvazars.arch.core.ViewModelBinder
import com.kvazars.arch.core.setBindings
import io.reactivex.Observable

fun <T> ViewModelBinder.bind(
    items: Observable<List<T>>,
    recyclerView: RecyclerView,
    @LayoutRes layoutId: Int,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false),
    initializer: SimpleViewHolder.(adapter: PluggableRecyclerViewAdapter<T, SimpleViewHolder>) -> Unit = {},
    binder: SimpleViewHolder.(adapter: PluggableRecyclerViewAdapter<T, SimpleViewHolder>, item: T, position: Int) -> Unit
) {
    val adapter = PluggableRecyclerViewAdapter<T, SimpleViewHolder>(
        createViewHolderDelegate = { layoutInflater, parent, _, adapter ->
            SimpleViewHolder(layoutInflater.inflate(layoutId, parent, false)).apply {
                initializer(adapter)
            }
        },
        bindViewHolderDelegate = { viewHolder, item, position, adapter -> viewHolder.binder(adapter, item, position) }
    )
    recyclerView.layoutManager = layoutManager
    recyclerView.adapter = adapter

    recyclerView.setBindings {
        bind(items) {
            adapter.submitList(it)
        }
    }
}
