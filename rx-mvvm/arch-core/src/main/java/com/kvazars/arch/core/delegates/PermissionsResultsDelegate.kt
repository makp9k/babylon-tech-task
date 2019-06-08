package com.kvazars.arch.core.delegates

import com.jakewharton.rxrelay2.PublishRelay
import com.kvazars.arch.core.utils.bufferWhileIdle
import io.reactivex.Observable

class PermissionsResultsDelegate(idleStates: Observable<Boolean>) {

    data class PermissionsResult(val requestCode: Int, val permissions: Array<out String>, val grantResults: IntArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PermissionsResult

            if (requestCode != other.requestCode) return false
            if (!permissions.contentEquals(other.permissions)) return false
            if (!grantResults.contentEquals(other.grantResults)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = requestCode
            result = 31 * result + permissions.contentHashCode()
            result = 31 * result + grantResults.contentHashCode()
            return result
        }
    }

    private val permissionResultsRelay = PublishRelay.create<PermissionsResult>()
    val permissionResults = permissionResultsRelay
        .bufferWhileIdle(idleStates)
        .publish()
        .apply { connect() }

    fun handlePermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionResultsRelay.accept(PermissionsResult(requestCode, permissions, grantResults))
    }

}
