@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.kvazars.arch.core.utils

import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function

inline fun <T> Relay<T>.asObservable(): Observable<T> {
    return this.hide()
}

inline fun <T> Relay<T>.asConsumer(): Consumer<T> {
    return this
}

inline fun <T> Observable<T>.skipWhileInProgress(progressState: Observable<Boolean>): Observable<T> {
    return this
        .withLatestFrom(
            progressState.startWith(false),
            BiFunction { t: T, inProgress: Boolean ->
                Pair(t, inProgress)
            }
        )
        .filter { !it.second }
        .map { it.first }
}

inline fun <T> Observable<T>.bufferWhileIdle(
    isIdle: Observable<Boolean>,
    bufferSize: Int? = null
): Observable<T> {

    val itemsObservable =
        this
            .withLatestFrom(
                isIdle,
                BiFunction { t: T, idle: Boolean -> Pair(t, idle) }
            )
            .publish()
            .refCount(2)

    return Observable
        .merge(
            itemsObservable
                .filter { (_, isIdle) -> isIdle.not() }
                .map { (item, _) -> item },

            itemsObservable
                .filter { (_, isIdle) -> isIdle }
                .map { (item, _) -> item }
                .buffer(
                    isIdle
                        .distinctUntilChanged()
                        .filter { it },
                    Function<Boolean, Observable<Boolean>> {
                        isIdle
                            .distinctUntilChanged()
                            .filter { it.not() }
                    }
                )
                .flatMapIterable {
                    if (bufferSize != null) it.takeLast(bufferSize) else it
                }
        )
}
