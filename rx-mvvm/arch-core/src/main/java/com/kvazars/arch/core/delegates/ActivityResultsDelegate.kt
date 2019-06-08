package com.kvazars.arch.core.delegates

import android.content.Intent
import com.jakewharton.rxrelay2.PublishRelay
import com.kvazars.arch.core.utils.bufferWhileIdle
import io.reactivex.Observable

class ActivityResultsDelegate(idleStates: Observable<Boolean>) {

    data class ActivityResult(val requestCode: Int, val resultCode: Int, val data: Intent?)

    private val activityResultsRelay = PublishRelay.create<ActivityResult>()
    val activityResults = activityResultsRelay
        .bufferWhileIdle(idleStates)
        .publish()
        .apply { connect() }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        activityResultsRelay.accept(ActivityResult(requestCode, resultCode, data))
    }

}
