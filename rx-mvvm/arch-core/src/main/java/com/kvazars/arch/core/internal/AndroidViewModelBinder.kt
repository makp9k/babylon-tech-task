package com.kvazars.arch.core.internal

import android.view.View
import com.kvazars.arch.core.ViewModelBinder
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference

internal class AndroidViewModelBinder(
    view: View
) : ViewModelBinder {

    private val compositeUnbind = CompositeDisposable()

    private var binderRef = WeakReference<(ViewModelBinder.() -> Unit)>(null)
    private var wasDetached = false

    private val attachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(v: View?) {
            wasDetached = true
            unbind()
        }

        override fun onViewAttachedToWindow(v: View?) {
            if (wasDetached) {
                wasDetached = false
                executePendingBindings()
            }
        }
    }

    init {
        view.addOnAttachStateChangeListener(attachStateChangeListener)

        if (view.isAttachedToWindow) {
            executePendingBindings()
        }
    }

    fun setBinder(binder: ViewModelBinder.() -> Unit) {
        binderRef = WeakReference(binder)
        executePendingBindings()
    }

    override fun forceBind() {
        binderRef.get()?.let {
            it()
        }
    }

    override fun unbind() {
        compositeUnbind.clear()
    }

    override fun Disposable.untilUnbind() {
        compositeUnbind.add(this)
    }

    private fun executePendingBindings() {
        forceBind()
    }

}
