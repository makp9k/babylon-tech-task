@file:Suppress("unused")

package com.kvazars.arch.core.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kvazars.arch.core.LibViewModel
import com.kvazars.arch.core.delegates.ViewDelegate

abstract class LibActivity<VM : LibViewModel> : AppCompatActivity() {

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

    override fun onDestroy() {
        viewDelegate.destroyView()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewDelegate.passActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewDelegate.passPermissionsResult(requestCode, permissions, grantResults)
    }

}
