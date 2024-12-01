package com.example.bluetoothapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bluetoothapp.data.Device
import com.example.bluetoothapp.ui.theme.BluetoothAppTheme
import kotlinx.coroutines.launch

@Composable
fun BluetoothApp(
    scan: () -> Unit,
    devices: List<Device>,
) {

    val scope = rememberCoroutineScope()

    Box() {
        IconButton(
            onClick ={
                scope.launch(){
                    scan()
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        devices.forEach { device ->
            key(device.address) {
                Text(
                    text = device.name.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
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