package com.example.bluetoothapp.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.bluetoothapp.data.Device
import com.example.bluetoothapp.ui.theme.BluetoothAppTheme

@Composable
fun BluetoothApp(
    scan: () -> Unit,
    devices: List<Device>,
) {
    Surface(
        modifier = Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        ConnectScreen(
            devices = devices,
            scan = scan,
            connectTo = { }
        )
    }
}

@Preview
@Composable
fun BluetoothAppPreview(){
    BluetoothAppTheme {
        Surface {
            BluetoothApp(scan = { }, devices = emptyList())
        }
    }
}