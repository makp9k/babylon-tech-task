@file:Suppress("unused")

package com.kvazars.arch.controls.dialog

import android.app.Dialog
import com.kvazars.arch.core.ViewModelBinder
import io.reactivex.android.schedulers.AndroidSchedulers

typealias DialogCreator<T, R> = (data: T, dc: DialogControl<T, R>) -> Dialog

fun <Data, Result> ViewModelBinder.bind(dialogControl: DialogControl<Data, Result>, dialogCreator: DialogCreator<Data, Result>) {
    var dialog: Dialog? = null

    val closeDialog: () -> Unit = {
        dialog?.setOnDismissListener(null)
        dialog?.dismiss()
        dialog = null
    }

    dialogControl.displayed.observable
        .observeOn(AndroidSchedulers.mainThread())
        .doFinally { closeDialog() }
        .subscribe {
            @Suppress("UNCHECKED_CAST")
            if (it is DialogControl.State.Displayed<*>) {
                dialog = dialogCreator(it.data as Data, dialogControl)
                dialog?.setOnDismissListener { i -> i.dismiss() }
                dialog?.show()
            } else if (it === com.kvazars.arch.controls.dialog.DialogControl.State.NotDisplayed) {
                closeDialog()
            }
        }
        .untilUnbind()
}
