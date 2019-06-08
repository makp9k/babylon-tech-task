@file:Suppress("unused")

package com.kvazars.arch.controls.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kvazars.arch.core.ViewModelBinder
import com.kvazars.arch.core.base.LibActivity
import com.kvazars.arch.core.base.LibFragment
import com.kvazars.arch.core.delegates.ViewDelegate
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlin.math.absoluteValue

fun ViewModelBinder.bind(runtimePermissionControl: RuntimePermissionControl, fragment: LibFragment<*>) {
    bind(runtimePermissionControl, fragment.viewDelegate, fragment.requireContext()) { permissions, requestCode ->
        fragment.requestPermissions(permissions, requestCode)
    }
}

fun ViewModelBinder.bind(runtimePermissionControl: RuntimePermissionControl, activity: LibActivity<*>) {
    bind(runtimePermissionControl, activity.viewDelegate, activity) { permissions, requestCode ->
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }
}

private fun ViewModelBinder.bind(runtimePermissionControl: RuntimePermissionControl,
                                 viewDelegate: ViewDelegate<*>,
                                 context: Context,
                                 permissionRequest: (permissions: Array<out String>, requestCode: Int) -> Unit) {
    val permissions = runtimePermissionControl.permissions
    val requestCode = permissions.contentHashCode().absoluteValue % (2 shl 15)

    viewDelegate.permissionResults
        .filter { it.requestCode == requestCode }
        .map { permissionsResult ->
            if (permissionsResult.grantResults.find { it == PackageManager.PERMISSION_DENIED } == null) {
                RuntimePermissionControl.PermissionsResult.Granted
            } else {
                RuntimePermissionControl.PermissionsResult.Denied
            }
        }
        .subscribe {
            runtimePermissionControl.sendResult(it)
        }
        .untilUnbind()

    runtimePermissionControl.requestCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            val allPermissionsGranted = permissions.find { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            } == null

            if (allPermissionsGranted) {
                runtimePermissionControl.sendResult(RuntimePermissionControl.PermissionsResult.Granted)
            } else {
                permissionRequest(permissions, requestCode)
            }
        }
        .untilUnbind()
}
