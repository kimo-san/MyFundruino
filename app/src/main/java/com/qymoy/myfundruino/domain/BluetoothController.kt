package com.qymoy.myfundruino.domain

import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

    val scannedDevices: StateFlow<List<BluetoothDevice>>

    suspend fun connect(device: BluetoothDevice)
    suspend fun disconnect(device: BluetoothDevice)

    val connectedTo: StateFlow<BluetoothDevice?>

    val messages: StateFlow<String>
    suspend fun sendMessage(mess: String): Boolean

    fun startDiscovery()
    fun stopDiscovery()
}