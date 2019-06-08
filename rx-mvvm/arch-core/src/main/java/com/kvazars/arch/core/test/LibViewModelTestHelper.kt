package com.kvazars.arch.core.test

import androidx.annotation.VisibleForTesting
import com.kvazars.arch.core.LibViewModel

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun LibViewModel.createAndBind() {
    create()
    bind()
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun LibViewModel.unbindAndDestroy() {
    unbind()
    clear()
}
