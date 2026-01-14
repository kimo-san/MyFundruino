package com.qymoy.myfundruino.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.qymoy.myfundruino.charUuid
import com.qymoy.myfundruino.domain.BluetoothController
import com.qymoy.myfundruino.domain.BluetoothDeviceDomain
import com.qymoy.myfundruino.hasPermission
import com.qymoy.myfundruino.serviceUuid
import com.qymoy.myfundruino.toDomain
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
): BluetoothController {

    private val deviceGatt = MutableStateFlow<BluetoothGatt?>(null)

    override val scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val messages = MutableStateFlow("")
    override val connectedTo = MutableStateFlow<BluetoothDeviceDomain?>(null)


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun connect(device: com.qymoy.myfundruino.domain.BluetoothDevice) {
        bluetoothAdapter?.getRemoteDevice(device.address)?.bluetoothGatt()
    }

    override suspend fun disconnect(device: com.qymoy.myfundruino.domain.BluetoothDevice) {
        connectedTo.update { null }
        deviceGatt.value?.close()
    }

    private suspend fun BluetoothDevice.bluetoothGatt(): Result<BluetoothGatt> =
        suspendCancellableCoroutine { continuation ->

            val callback = object : GattCallback(
                gattFlow = deviceGatt,
                messagesFlow = messages,
                context = context
            ) {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    super.onConnectionStateChange(gatt, status, newState)
                    when (status) {
                        BluetoothGatt.GATT_SUCCESS -> {
                            connectedTo.update { gatt.device.toDomain() }
                            continuation.resume(Result.success(gatt))
                        }
                        BluetoothGatt.GATT_FAILURE -> {
                            connectedTo.update { null }
                            continuation.resume(Result.failure(Exception("Connection failed")))
                        }
                    }
                }
            }

            val gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) connectGatt(
                context, // context
                false, // autoConnect
                callback, // callback
                BluetoothDevice.TRANSPORT_AUTO, // transport
                BluetoothDevice.PHY_LE_1M // phy
            )
            else connectGatt(context, false, callback)

            continuation.invokeOnCancellation { gatt.close() }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun sendMessage(mess: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            val gatt = deviceGatt.value ?: error("caught! there is no gatt!")

            val char = gatt
                .getService(serviceUuid)
                .getCharacteristic(charUuid)

            println(char.writeType)

            gatt.writeCharacteristic(
                char,
                mess.toByteArray(Charsets.US_ASCII),
                char.writeType
            )

            val received = gatt.readCharacteristic(char)

            continuation.resume(received)
            continuation.invokeOnCancellation {
                println("Delivered: canceled!")
            }
        }

    override fun startDiscovery() {
        if (!context.hasPermission(Manifest.permission.BLUETOOTH_SCAN))
            return

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(foundDeviceReceiver, filter)

        bluetoothAdapter?.startDiscovery()
    }
    override fun stopDiscovery() {
        if (!context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
            return
        context.unregisterReceiver(foundDeviceReceiver)

        bluetoothAdapter?.cancelDiscovery()
    }

    private val bluetoothManager by lazy { context.getSystemService(BluetoothManager::class.java) }
    private val bluetoothAdapter by lazy { bluetoothManager?.adapter }
    private val foundDeviceReceiver by lazy {
        FoundDeviceReceiver { device ->
            scannedDevices.update { devices ->
                val newDevice: BluetoothDeviceDomain = device.toDomain()
                if (newDevice in devices) devices else devices + newDevice
            }
        }
    }
}