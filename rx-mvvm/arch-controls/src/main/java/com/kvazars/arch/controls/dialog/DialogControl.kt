@file:Suppress("unused")

package com.kvazars.arch.controls.dialog

import com.kvazars.arch.core.LibViewModel
import com.kvazars.arch.core.WidgetControl
import io.reactivex.Maybe

class DialogControl<Data, Result> internal constructor(vm: LibViewModel) : WidgetControl {

    sealed class State {
        class Displayed<T>(val data: T) : State()
        object NotDisplayed : State()
    }

    sealed class GenericDialogResult {
        object Accepted : GenericDialogResult()
        object Declined : GenericDialogResult()
    }

    val displayed = vm.State<State>(State.NotDisplayed)
    private val result = vm.Action<Result>()

    fun show(data: Data) {
        dismiss()
        displayed.relay.accept(State.Displayed(data))
    }

    fun showForResult(data: Data): Maybe<Result> {
        dismiss()

        return result.relay
            .doOnSubscribe {
                displayed.relay.accept(State.Displayed(data))
            }
            .takeUntil(
                displayed.relay
                    .skip(1)
                    .filter { it == State.NotDisplayed }
            )
            .firstElement()
    }

    fun sendResult(result: Result) {
        this.result.consumer.accept(result)
        dismiss()
    }

    fun dismiss() {
        if (displayed.valueOrNull is State.Displayed<*>) {
            displayed.relay.accept(State.NotDisplayed)
        }
    }
}

fun <Data, Result> LibViewModel.dialogControl(): DialogControl<Data, Result> {
    return DialogControl(this)
}
