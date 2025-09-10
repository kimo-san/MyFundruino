package com.qymoy.myfundruino.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qymoy.myfundruino.domain.BluetoothDevice

@Composable
fun MainContent(
    vm: MainViewModel
) {

    val state by vm.state.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->

        Column(Modifier.padding(innerPadding)) {

            if (state.connected != null) {

                ConnectedDevice(
                    device = state.connected!!,
                    send = vm::send,
                    disconnect = vm::disconnect
                )

                Terminal(
                    messages = vm.messages.collectAsState().value
                )

            }
            else {

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                    TextButton(vm::stopDisc) {
                        Text("Stop scan")
                    }

                    TextButton(vm::startDisc) {
                        Text("Enable scan")
                    }
                }

                HorizontalDivider()

                DevList(
                    list = state.scannedDevs,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    connectToDevice = vm::connectTo
                )
            }
        }
    }
}

@Composable
private fun Terminal(
    messages: List<String>
) {
    LazyColumn {
        items(messages) {
            ListItem({ Text(it) }, Modifier.animateItem())
        }
    }
}

@Composable
private fun ConnectedDevice(
    device: BluetoothDevice,
    send: (String) -> Unit,
    disconnect: (BluetoothDevice) -> Unit,
) {
    Card(Modifier.fillMaxWidth().safeContentPadding()) {

        Text(device.name ?: "Unnamed")
        Text(device.address)

        var message by remember { mutableStateOf("type a message...") }
        TextField(
            message, { message = it },
            Modifier.fillMaxWidth(),
            trailingIcon = {
                Button({ send(message) }) {
                    Text("Send")
                }
            }
        )

        Row {
            TextButton({ disconnect(device) }) {
                Text("Disconnect")
            }
        }
    }
}

@Composable
private fun DevList(
    modifier: Modifier = Modifier,
    list: List<BluetoothDevice>,
    connectToDevice: (BluetoothDevice) -> Unit
) {

    LazyColumn(modifier) {

        items(list) {
            NavigationDrawerItem(
                label = { Text("${it.name} (${it.address})") },
                selected = false,
                onClick = { connectToDevice(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}