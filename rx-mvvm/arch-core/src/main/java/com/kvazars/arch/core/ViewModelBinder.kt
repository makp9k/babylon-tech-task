package com.kvazars.arch.core

import android.view.View
import com.kvazars.arch.core.internal.AndroidViewModelBinder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

internal fun View.getViewModelBinder(): AndroidViewModelBinder? {
    return getTag(R.id.rxDataBinding) as? AndroidViewModelBinder
}

internal fun View.setViewModelBinder(binder: AndroidViewModelBinder) {
    return setTag(R.id.rxDataBinding, binder)
}

fun View.setBindings(binder: ViewModelBinder.() -> Unit) {
    var viewModelBinder = getViewModelBinder()
    if (viewModelBinder == null) {
        viewModelBinder = AndroidViewModelBinder(this)
        setViewModelBinder(viewModelBinder)
    }
    viewModelBinder.setBinder(binder)
}

interface ViewModelBinder {

    fun unbind()

    fun forceBind()

    fun Disposable.untilUnbind()

    fun <T> bind(observable: Observable<T>, consumer: Consumer<in T>) {
        observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(consumer)
            .untilUnbind()
    }

    fun <T> bind(observable: Observable<T>, consumer: (T) -> Unit) {
        observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(consumer)
            .untilUnbind()
    }

    fun <T> bind(observable: Observable<T>, action: LibViewModel.Action<T>) = bind(observable, action.consumer)

    fun <T> bind(state: LibViewModel.State<T>, consumer: Consumer<in T>) = bind(state.observable, consumer)

    fun <T> bind(state: LibViewModel.State<T>, consumer: (T) -> Unit) = bind(state.observable, consumer)

    fun <T> bind(command: LibViewModel.Command<T>, consumer: Consumer<in T>) = bind(command.observable, consumer)

    fun <T> bind(command: LibViewModel.Command<T>, consumer: (T) -> Unit) = bind(command.observable, consumer)

}