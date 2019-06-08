@file:Suppress("unused")

package com.kvazars.arch.controls.permissions

import com.kvazars.arch.core.LibViewModel
import com.kvazars.arch.core.WidgetControl
import io.reactivex.Single

class RuntimePermissionControl internal constructor(
    vm: LibViewModel,
    val permissions: Array<out String>
) : WidgetControl {

    sealed class PermissionsResult {
        object Granted : PermissionsResult()
        object Denied : PermissionsResult()
    }

    internal val requestCommand = vm.Command<Unit>(1)
    private val permissionsResult = vm.Action<PermissionsResult>()

    fun request(): Single<PermissionsResult> {
        return permissionsResult.relay
            .doOnSubscribe { requestCommand.relay.accept(Unit) }
            .first(PermissionsResult.Denied)
    }

    fun sendResult(result: PermissionsResult) {
        permissionsResult.relay.accept(result)
    }
}

fun LibViewModel.runtimePermissionsControl(vararg permissions: String): RuntimePermissionControl {
    return RuntimePermissionControl(this, permissions)
}
