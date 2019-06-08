package com.kvazars.arch.controls.list.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

class SimpleViewHolder(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer
