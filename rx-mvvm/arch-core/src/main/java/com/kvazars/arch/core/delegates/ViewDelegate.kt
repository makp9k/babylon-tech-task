@file:Suppress("unused")

package com.kvazars.arch.core.delegates

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kvazars.arch.core.LibViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class ViewDelegate<VM : LibViewModel> private constructor(
    screenKey: String,
    viewModelProvider: ViewModelProvider
) {
    companion object {

        @Suppress("unchecked_cast")
        fun <VM : LibViewModel> of(
            key: String,
            viewModelStore: ViewModelStore,
            viewModelCreator: () -> VM
        ): ViewDelegate<VM> {
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val viewModel = viewModelCreator()
                    viewModel.onCreate()
                    return viewModel as T
                }
            }

            return ViewDelegate(
                key,
                ViewModelProvider(viewModelStore, factory)
            )
        }
    }

    @Suppress("unchecked_cast")
    val viewModel = viewModelProvider.get(screenKey, LibViewModel::class.java) as VM

    val isIdle = BehaviorRelay.createDefault(true)

    private val activityResultsDelegate = ActivityResultsDelegate(isIdle)
    val activityResults = activityResultsDelegate.activityResults

    private val permissionResultsDelegate = PermissionsResultsDelegate(isIdle)
    val permissionResults = permissionResultsDelegate.permissionResults

    private val compositeDestroy = CompositeDisposable()
    private var viewBound = false

    fun bindView() {
        if (!viewBound) {
            viewBound = true
            viewModel.bind()
            isIdle.accept(false)
        }
    }

    fun unbindView() {
        if (viewBound) {
            viewBound = false
            isIdle.accept(true)
            viewModel.unbind()
        }
    }

    fun destroyView() {
        compositeDestroy.clear()
    }

    fun untilDestroy(disposable: Disposable) {
        compositeDestroy.add(disposable)
    }

    fun passActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        activityResultsDelegate.handleActivityResult(requestCode, resultCode, data)
    }

    fun passPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionResultsDelegate.handlePermissionsResult(requestCode, permissions, grantResults)
    }
}
