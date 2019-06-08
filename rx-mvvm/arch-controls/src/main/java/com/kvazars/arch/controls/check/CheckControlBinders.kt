@file:Suppress("unused")

package com.kvazars.arch.controls.check

import android.widget.CompoundButton
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.kvazars.arch.core.ViewModelBinder
import com.kvazars.arch.core.setBindings
import io.reactivex.android.schedulers.AndroidSchedulers

fun ViewModelBinder.bind(checkControl: CheckControl, compoundButton: CompoundButton) {
    var editing = false

    compoundButton.setBindings {
        checkControl.checked.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                editing = true
                compoundButton.isChecked = it
                editing = false
            }
            .untilUnbind()

        compoundButton
            .checkedChanges()
            .skipInitialValue()
            .filter { !editing }
            .subscribe(checkControl.checkedChanges.consumer)
            .untilUnbind()
    }
}
