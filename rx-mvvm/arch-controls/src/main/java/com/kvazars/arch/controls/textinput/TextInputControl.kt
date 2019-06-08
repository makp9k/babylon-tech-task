@file:Suppress("unused")

package com.kvazars.arch.controls.textinput

import com.kvazars.arch.core.LibViewModel
import com.kvazars.arch.core.WidgetControl

class TextInputControl internal constructor(
    vm: LibViewModel,
    initialText: CharSequence,
    formatter: (text: CharSequence) -> CharSequence,
    hideErrorOnUserInput: Boolean
) : WidgetControl {

    val text = vm.State(initialText)
    @SuppressWarnings("WeakerAccess")
    val error = vm.State<CharSequence>()
    val textChanges = vm.Action<String>()

    init {
        @Suppress("CheckResult")
        textChanges.relay
            .filter { it != text.value }
            .map { formatter.invoke(it) }
            .subscribe {
                if (hideErrorOnUserInput) {
                    error.relay.accept("")
                }
                text.relay.accept(it)
            }
    }
}

fun LibViewModel.textInputControl(
    initialText: String = "",
    formatter: (text: CharSequence) -> CharSequence = { it },
    hideErrorOnUserInput: Boolean = true
): TextInputControl {
    return TextInputControl(this, initialText, formatter, hideErrorOnUserInput)
}
