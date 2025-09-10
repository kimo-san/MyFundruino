package com.qymoy.myfundruino

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import com.qymoy.myfundruino.domain.BluetoothDeviceDomain
import java.util.UUID

const val TAG = "debu"

val serviceUuid = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
val charUuid = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")
val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

fun Context.hasPermission(permission: String): Boolean =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
fun BluetoothDevice.toDomain(): BluetoothDeviceDomain =
    com.qymoy.myfundruino.domain.BluetoothDeviceDomain(
        name = try {
            name
        } catch (_: Throwable) {
            "(permission BLUETOOTH_CONNECT required)"
        },
        address = address
    )