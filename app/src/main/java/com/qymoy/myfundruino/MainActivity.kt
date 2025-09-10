package com.qymoy.myfundruino

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.qymoy.myfundruino.data.AndroidBluetoothController
import com.qymoy.myfundruino.presentation.MainContent
import com.qymoy.myfundruino.presentation.MainViewModel
import com.qymoy.myfundruino.ui.theme.MyFundruinoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {



    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager by lazy {
            applicationContext.getSystemService(BluetoothManager::class.java)
        }
        val bluetoothAdapter by lazy {
            bluetoothManager?.adapter
        }

        val isBleEnabl: Boolean = bluetoothAdapter?.isEnabled == true

        val enableBleLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { /* not needed */ }

        val permLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val canEnBle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            if (canEnBle && !isBleEnabl) {
                enableBleLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }



        enableEdgeToEdge()
        setContent {

            MyFundruinoTheme {

                val vm by viewModels<MainViewModel> {
                    MainViewModel.factory(
                        AndroidBluetoothController(applicationContext)
                    )
                }

                MainContent(vm)
            }
        }
    }
}