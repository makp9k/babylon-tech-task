package com.example.babylon.posts.collection

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MaintainingScrollPositionRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private val dataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            val pos = savedPosition
            val offset = savedOffset
            val lm = layoutManager as? LinearLayoutManager
            if (pos != null && offset != null && lm != null) {
                lm.scrollToPositionWithOffset(pos, offset)
            }
            savedPosition = null
            savedOffset = null
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onChanged()
        }
    }

    private var savedPosition: Int? = null
    private var savedOffset: Int? = null

    override fun setAdapter(adapter: Adapter<*>?) {
        this.adapter?.unregisterAdapterDataObserver(dataObserver)
        super.setAdapter(adapter)
        this.adapter?.registerAdapterDataObserver(dataObserver)
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = Bundle()
        state.putParcelable("recycler", super.onSaveInstanceState())
        (layoutManager as? LinearLayoutManager)?.let {
            state.putInt("position", it.findFirstVisibleItemPosition())
            val startView = getChildAt(0)
            state.putInt("offset", if (startView != null) startView.top - paddingTop else 0)
        }
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("recycler"))
        savedPosition = bundle.getInt("position")
        savedOffset = bundle.getInt("offset")
    }
}
