package com.qymoy.myfundruino.presentation

import com.qymoy.myfundruino.domain.BluetoothDevice

data class BluetoothUiState(
    val connected: BluetoothDevice? = null,
    val scannedDevs: List<BluetoothDevice> = emptyList()
)