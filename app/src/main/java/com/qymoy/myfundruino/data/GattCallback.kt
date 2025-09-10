package com.qymoy.myfundruino.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.qymoy.myfundruino.cccdUuid
import com.qymoy.myfundruino.charUuid
import com.qymoy.myfundruino.hasPermission
import com.qymoy.myfundruino.serviceUuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlin.collections.find
import kotlin.error

open class GattCallback(

    private val context: Context,

    private val gattFlow: FlowCollector<BluetoothGatt>,
    private val messagesFlow: FlowCollector<String>,

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

): BluetoothGattCallback() {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) scope.launch {
            gattFlow.emit(gatt)
            gatt.discoverServices()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            val services = gatt!!.services

            val service = services.find { it.uuid == serviceUuid }
                ?: error("caught! there is no service!")

            if (context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
                enableNotifications(gatt, service)
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        scope.launch {
            messagesFlow.emit(value.toString(Charsets.US_ASCII))
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun enableNotifications(
        gatt: BluetoothGatt,
        service: BluetoothGattService
    ) {
        val char = service.getCharacteristic(charUuid)
            ?: error("caught! there are no characteristics!")

        val okSet = gatt.setCharacteristicNotification(char, true)
        if (!okSet) error("caught! there are no notifications!")

        val desc = char.getDescriptor(cccdUuid)
            ?: error("caught! there is no descriptor!")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            gatt.writeDescriptor(
                desc,
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            )
        else
            gatt.writeDescriptor(
                desc.apply {
                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                }
            )
    }
}