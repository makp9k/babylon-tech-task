package com.kvazars.arch.core

import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable

interface WidgetControl {

    val <T> LibViewModel.State<T>.relay: Relay<T>
        get() = relay

    val <T> LibViewModel.Command<T>.relay: Relay<T>
        get() = relay

    val <T> LibViewModel.Action<T>.relay: Relay<T>
        get() = relay

    val LibViewModel.lifecycleEvents: Observable<LibViewModel.Lifecycle>
        get() = lifecycleEvents

}
