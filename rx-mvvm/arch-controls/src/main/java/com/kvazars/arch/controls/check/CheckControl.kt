@file:Suppress("unused")

package com.kvazars.arch.controls.check

import com.kvazars.arch.core.LibViewModel
import com.kvazars.arch.core.WidgetControl

class CheckControl internal constructor(vm: LibViewModel, initialChecked: Boolean) :
    WidgetControl {

    val checked = vm.State(initialChecked)
    val checkedChanges = vm.Action<Boolean>()

    init {
        @Suppress("CheckResult")
        checkedChanges.relay
            .filter { it != checked.value }
            .subscribe(checked.relay)
    }
}

fun LibViewModel.checkControl(initialChecked: Boolean = false): CheckControl {
    return CheckControl(this, initialChecked)
}
