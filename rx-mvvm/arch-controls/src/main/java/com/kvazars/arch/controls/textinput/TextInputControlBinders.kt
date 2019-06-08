@file:Suppress("unused")

package com.kvazars.arch.controls.textinput

import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.jakewharton.rxbinding3.widget.textChanges
import com.kvazars.arch.core.ViewModelBinder
import com.kvazars.arch.core.setBindings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

private fun bind(
    textInputControl: TextInputControl,
    view: View,
    textChanges: Observable<CharSequence>,
    textReader: () -> CharSequence,
    textWriter: (CharSequence) -> Unit,
    errorHandler: (CharSequence) -> Unit
) {
    var editing = false

    view.setBindings {
        textInputControl.text.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val currentText = textReader()
                if (!android.text.TextUtils.equals(it, currentText)) {
                    editing = true
                    textWriter(it)
                    editing = false
                }
            }
            .untilUnbind()

        textChanges
            .filter { !editing }
            .map { it.toString() }
            .subscribe(textInputControl.textChanges.consumer)
            .untilUnbind()

        textInputControl.error.observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                errorHandler(it)
            }
            .untilUnbind()
    }
}

fun ViewModelBinder.bind(textInputControl: TextInputControl, editText: EditText) {
    bind(
        textInputControl,
        editText,
        editText.textChanges().skipInitialValue(),
        { editText.text },
        {
            val editable = editText.text
            if (editable is Spanned) {
                val ss = SpannableString(it)
                TextUtils.copySpansFrom(editable, 0, ss.length, null, ss, 0)
                editable.replace(0, editable.length, ss)
            } else {
                editable.replace(0, editable.length, it)
            }
        },
        {}
    )
}

fun ViewModelBinder.bind(textInputControl: TextInputControl, textInput: TextInputEditText) {
    bind(
        textInputControl,
        textInput,
        textInput.textChanges().skipInitialValue(),
        { textInput.text ?: "" },
        {
            val editable = textInput.text
            if (editable is Spanned) {
                val ss = SpannableString(it)
                TextUtils.copySpansFrom(editable, 0, ss.length, null, ss, 0)
                editable.replace(0, editable.length, ss)
            } else {
                editable?.replace(0, editable.length, it)
            }
        },
        {
            textInput.error = if (it.isNotBlank()) it else null
        }
    )
}
