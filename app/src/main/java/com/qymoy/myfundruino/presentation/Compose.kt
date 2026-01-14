package com.qymoy.myfundruino.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qymoy.myfundruino.R
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

                ConnectedScreen(
                    device = state.connected!!,
                    send = { vm.send(it.cmd) },
                    disconnect = vm::disconnect,
                    terminalText = vm.messages.collectAsState().value
                )

            }
            else {

                var scanning by remember { mutableStateOf(false) }
                if (scanning) {
                    TextButton({ vm.stopDisc(); scanning = false }) {
                        Text("Stop scan")
                    }
                } else {
                    TextButton({ vm.startDisc(); scanning = true }) {
                        Text("Enable scan")
                    }
                }

                HorizontalDivider()

                DeviceList(
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

enum class Commands(val cmd: String, val displayName: String) {
    EXECUTE_CMD("d", "Execute main program"),
    CREAM_TEST_CMD("r", "Cream-out test"),
    WATER_TEST_CMD("w", "Water-in test"),
    PIN_TEST_CMD("p", "Pin test"),
    CANCEL_CMD("c", "Cancel execution"),
}

@Composable
fun ConnectedScreen(
    device: BluetoothDevice,
    send: (Commands) -> Unit,
    disconnect: (BluetoothDevice) -> Unit,
    terminalText: List<String>
) {
    Column(Modifier
        .fillMaxSize()
        .safeContentPadding(),
        Arrangement.SpaceAround) {

        Column {
            Text(
                "Connected to " + (device.name ?: "Unnamed"),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                device.address,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Image(
            painterResource(R.drawable.birds_for_beginners), null,
            Modifier.fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        LazyColumn(
            contentPadding = PaddingValues(8.dp)
        ) {
            items(Commands.entries) {
                Button({ send(it) }) {
                    Text(
                        it.displayName,
                        Modifier.fillMaxWidth(),
                        maxLines = 1
                    )
                }
            }
        }

        Terminal(terminalText)

        OutlinedButton({ send(Commands.CANCEL_CMD); disconnect(device) }) {
            Text("Disconnect")
        }
    }
}

@Composable
fun Terminal(text: List<String>) {
    Card(
        Modifier
            .height(200.dp)
    ) {
        val state = rememberScrollState()
        Row {

            Text(
                text.joinToString(separator = "") { it },
                Modifier
                    .weight(1f)
                    .verticalScroll(state)
                    .padding(16.dp),
                overflow = TextOverflow.Visible
            )

            Column(
                Modifier
                    .width(10.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .height(200.dp)
            ) {

                val safeRange = 1..state.maxValue.coerceAtLeast(1)

                Spacer(
                    Modifier
                        .weight(state.value.coerceIn(safeRange) * 1f)
                )

                Box(
                    Modifier
                        .weight(state.viewportSize.coerceIn(safeRange) * 1f)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clip(RoundedCornerShape(100))
                )

                Spacer(
                    Modifier
                        .weight(
                            (state.maxValue - state.value).coerceIn(safeRange) * 1f
                        )
                )
            }

        }
    }
}

@Composable
private fun DeviceList(
    modifier: Modifier = Modifier,
    list: List<BluetoothDevice>,
    connectToDevice: (BluetoothDevice) -> Unit
) {

    LazyColumn(modifier) {
        items(list) {
            ListItem(
                headlineContent = { Text(it.name ?: "Unnamed") },
                supportingContent = { Text(it.address) },
                modifier = Modifier.clickable { connectToDevice(it) }
            )
        }
    }
}