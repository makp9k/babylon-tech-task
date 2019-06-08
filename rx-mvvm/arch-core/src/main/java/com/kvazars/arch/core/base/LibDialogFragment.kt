@file:Suppress("unused")

package com.kvazars.arch.core.base

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.kvazars.arch.core.LibViewModel
import com.kvazars.arch.core.delegates.ViewDelegate

abstract class LibDialogFragment<VM : LibViewModel> : DialogFragment() {

    lateinit var viewDelegate: ViewDelegate<VM>

    protected val vm: VM by lazy(LazyThreadSafetyMode.NONE) { viewDelegate.viewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewDelegate = ViewDelegate.of(
            key = this.javaClass.canonicalName!!,
            viewModelStore = viewModelStore,
            viewModelCreator = ::createViewModel
        )

        super.onCreate(savedInstanceState)
    }

    abstract fun createViewModel(): VM

    override fun onStart() {
        super.onStart()
        viewDelegate.bindView()
    }

    override fun onStop() {
        viewDelegate.unbindView()
        super.onStop()
    }

    override fun onDestroyView() {
        viewDelegate.destroyView()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewDelegate.passActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        viewDelegate.passPermissionsResult(requestCode, permissions, grantResults)
    }

}
