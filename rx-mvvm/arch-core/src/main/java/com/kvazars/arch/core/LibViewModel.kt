@file:Suppress("unused")

package com.kvazars.arch.core

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.kvazars.arch.core.utils.asConsumer
import com.kvazars.arch.core.utils.asObservable
import com.kvazars.arch.core.utils.bufferWhileIdle
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.functions.Consumer
import io.reactivex.observables.ConnectableObservable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicReference

open class LibViewModel : ViewModel() {

    enum class Lifecycle {
        CREATE, BIND, UNBIND, DESTROY
    }

    private val lifecycle = BehaviorRelay.create<Lifecycle>()
    internal val lifecycleEvents: Observable<Lifecycle> = lifecycle.distinctUntilChanged()

    private val compositeDestroy = CompositeDisposable()
    private val compositeUnbind = CompositeDisposable()

    internal fun create() {
        if (lifecycle.value == Lifecycle.CREATE) {
            return
        }

        lifecycle.accept(Lifecycle.CREATE)
        onCreate()
    }

    open fun onCreate() {

    }

    internal fun bind() {
        if (lifecycle.value == Lifecycle.BIND) {
            return
        }

        lifecycle.accept(Lifecycle.BIND)
        onBind()
    }

    protected open fun onBind() {

    }

    internal fun unbind() {
        if (lifecycle.value == Lifecycle.UNBIND) {
            return
        }

        lifecycle.accept(Lifecycle.UNBIND)
        compositeUnbind.clear()
        onUnbind()
    }

    protected open fun onUnbind() {

    }

    internal fun clear() {
        onCleared()
    }

    override fun onCleared() {
        if (lifecycle.value == Lifecycle.DESTROY) {
            return
        }

        lifecycle.accept(Lifecycle.DESTROY)
        compositeDestroy.clear()
        super.onCleared()
    }

    fun Disposable.untilUnbind() {
        compositeUnbind.add(this)
    }

    fun Disposable.untilDestroy() {
        compositeDestroy.add(this)
    }

    fun <T> Observable<T>.bufferWhileUnbind(bufferSize: Int? = null): Observable<T> {
        return bufferWhileIdle(lifecycle.map { it != Lifecycle.BIND }.startWith(true), bufferSize)
    }

    fun <T> Observable<T>.listenUntilDestroy(callback: (T) -> Unit) {
        subscribe(callback).untilDestroy()
    }

    fun <T> Observable<T>.listenUntilUnbind(callback: (T) -> Unit) {
        subscribe(callback).untilUnbind()
    }

    fun <T> Single<T>.listenUntilDestroy(callback: (T) -> Unit) {
        subscribe(callback).untilDestroy()
    }

    fun <T> Single<T>.listenUntilUnbind(callback: (T) -> Unit) {
        subscribe(callback).untilUnbind()
    }

    protected fun <T> doInBackground(task:() -> T,
                                     onSuccess: (T) -> Unit = {},
                                     onError: (Throwable) -> Unit = {throw OnErrorNotImplementedException(it)}) {
        Single.fromCallable(task)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
            .untilDestroy()
    }

    /*
     *  State
     */

    val <T> State<T>.consumer: Consumer<T> get() = relay

    fun <T> State<T>.set(value: T) {
        consumer.accept(value)
    }

    fun <T> State<T>.listenUntilDestroy(callback: (T) -> Unit) {
        relay.subscribe(callback).untilDestroy()
    }

    fun <T> State<T>.listenUntilUnbind(callback: (T) -> Unit) {
        relay.subscribe(callback).untilUnbind()
    }

    inner class State<T>(initialValue: T? = null) {

        internal val relay =
            if (initialValue != null) {
                BehaviorRelay.createDefault<T>(initialValue).toSerialized()
            } else {
                BehaviorRelay.create<T>().toSerialized()
            }

        private val cachedValue =
            if (initialValue != null) {
                AtomicReference<T?>(initialValue)
            } else {
                AtomicReference()
            }

        val observable: Observable<T> = relay.asObservable()

        val value: T
            get() {
                return cachedValue.get()
                    ?: throw UninitializedPropertyAccessException("The State has no value yet. Use valueOrNull() or pass initialValue bindWith the constructor.")
            }

        val valueOrNull: T? get() = cachedValue.get()

        init {
            @Suppress("CheckResult")
            relay.subscribe {
                cachedValue.set(it)
            }
        }

        fun hasValue() = cachedValue.get() != null
    }

    /*
     *  Action
     */

    val <T> Action<T>.observable: Observable<T> get() = relay

    fun <T> Action<T>.listenUntilDestroy(callback: (T) -> Unit) {
        relay.subscribe(callback).untilDestroy()
    }

    fun <T> Action<T>.listenUntilUnbind(callback: (T) -> Unit) {
        relay.subscribe(callback).untilUnbind()
    }

    inner class Action<T> {
        internal val relay = PublishRelay.create<T>().toSerialized()

        val consumer get() = relay.asConsumer()

        fun fire(data: T) {
            consumer.accept(data)
        }
    }

    /*
     *  Command
     */

    val <T> Command<T>.consumer: Consumer<T> get() = relay

    fun <T> Command<T>.fire(data: T) {
        consumer.accept(data)
    }

    inner class Command<T>(
        bufferSize: Int? = null,
        isIdle: Observable<Boolean>? = null
    ) {
        internal val relay = PublishRelay.create<T>().toSerialized()

        val observable: ConnectableObservable<T> =
            if (bufferSize == 0) {
                relay.asObservable()
            } else {
                if (isIdle == null) {
                    relay.bufferWhileUnbind(bufferSize)
                } else {
                    relay.bufferWhileIdle(isIdle, bufferSize)
                }
            }
                .publish()
                .apply { connect() }
    }

}
