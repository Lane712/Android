package com.example.bluetoothapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bluetoothapp.R
import com.example.bluetoothapp.data.Device
import com.example.bluetoothapp.ui.theme.BluetoothAppTheme


@Composable
fun ConnectScreen(
    devices: List<Device>,
    scan: () -> Unit,
    connectTo: (Device) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text= stringResource(R.string.paired_devices),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium
            )
            devices.forEach { device ->
                if (device.connected) {
                    DeviceContent(device = device , connectTo = connectTo, color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text= stringResource(R.string.discovery_devices),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = scan) {
                    Icon(imageVector = Icons.Default.Refresh,contentDescription = null)
                }
            }
            devices.forEach { device ->
                if (!device.connected)
                DeviceContent(device = device,connectTo = connectTo, color = MaterialTheme.colorScheme.surfaceContainer )
            }
        }
    }
}

@Composable
fun DeviceContent(
    device: Device,
    color: Color,
    connectTo: (Device) -> Unit,
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .clickable(onClick = { connectTo(device) })
    ) {
        Image(
            painter = painterResource(R.drawable.bluetooth),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Text(
            text = device.name.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Text(
            text = device.address,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
fun ConnectScreenPreview(){
    BluetoothAppTheme {
        ConnectScreen(
            devices = defaultDevices ,
            scan = { },
            connectTo = { }
        )
    }
}

val defaultDevices = listOf(
    Device(name = "REDMI K80" , address = "00:22:SS:AA"),
    Device(name = "XIAOMI 13" , address = "NN:22:55:AA" ,connected = true),
    Device(name = "VIVO X100" , address = "A0:9F:S3:33"),
    Device(name = "HUAWEI mate60" , address = "00:AD:SE:AR"),
    Device(name = "OPPO K10" , address = "02:A4:4E:AR"),
    Device(name = "iPhone 12" , address = "76:A2:S0:22", connected = true),
)
