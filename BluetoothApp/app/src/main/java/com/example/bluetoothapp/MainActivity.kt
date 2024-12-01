package com.example.bluetoothapp

import android.Manifest.permission.BLUETOOTH_ADVERTISE
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import com.example.bluetoothapp.data.Device
import com.example.bluetoothapp.ui.BluetoothApp
import com.example.bluetoothapp.ui.theme.BluetoothAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.UUID

const val REQUEST_ENABLE_BT = 1
const val REQUEST_DISCOVERABLE_BT = 5

const val REQUEST_PREVIOUS_BT_P = 11
const val REQUEST_SCAN_BT_P = 12
const val REQUEST_CONNECT_BT_P = 13
const val REQUEST_ADVERTISE_BT_P = 14

const val REQUEST_PERMISSIONS_BT_P = 99

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // by lazy {...} 对象第一次被访问时创建
    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    lateinit var bluetoothService: BluetoothService
    lateinit var handler: Handler

    val viewModel: BluetoothViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handler = object : Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when(msg.what){
                    MESSAGE_READ -> {
                        // 处理收到的数据
                    }
                    MESSAGE_WRITE -> {
                        // 处理发送数据的反馈
                    }
                    MESSAGE_TOAST -> {
                        // 显示Toast消息
                        val bundle = msg.data
                        Toast.makeText(this@MainActivity,bundle.getString("toast"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 初始化蓝牙功能
        fun initialBluetooth(){
            // 检查蓝牙是否打开
            if (bluetoothAdapter?.isEnabled == false){
                // 请求打开蓝牙
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT)
            }
            // 注册发现蓝牙设备监听广播
            val deviceFoundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, deviceFoundFilter)

            //获取已配对设备
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.forEach { device ->
                viewModel.add(
                    Device(
                        name = device.name,
                        address = device.address,// MAC address
                        connected = true
                    )
                )
            }
        }

        // 检查并获取蓝牙相关权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            // API >= 31
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    this@MainActivity,BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    this@MainActivity,BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED
            ) {
                // 请求蓝牙权限
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(BLUETOOTH_SCAN,BLUETOOTH_CONNECT,BLUETOOTH_ADVERTISE), REQUEST_PERMISSIONS_BT_P)
            }else{
                // 蓝牙权限已获取
                initialBluetooth() // 初始化
            }
        }else {
            // API < 31
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,BLUETOOTH_SERVICE) != PackageManager.PERMISSION_GRANTED
            ){
                // 传统蓝牙权限获取
                ActivityCompat.requestPermissions(
                    this@MainActivity,arrayOf(BLUETOOTH_SERVICE),REQUEST_PREVIOUS_BT_P)
            }
            else{
                // 蓝牙权限已获取
                initialBluetooth() // 初始化
            }
        }

        setContent {
            val devices by viewModel.devices.collectAsState()

            BluetoothAppTheme {
                BluetoothApp(
                    scan = {
                        when(bluetoothAdapter?.isDiscovering){
                            true -> bluetoothAdapter?.cancelDiscovery()
                            else -> bluetoothAdapter?.startDiscovery()
                        }
                    },
                    devices = devices
                )
            }
        }

        /*
        注册蓝牙连接状态监听广播
        if (ActivityCompat.checkSelfPermission(
        this@MainActivity,BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
        // 权限请求
        ActivityCompat.requestPermissions(
        this@MainActivity, arrayOf(BLUETOOTH_CONNECT), REQUEST_CONNECT_BT_P)

        }else {
        // 检测蓝牙连接状态变化
        val connectionStateFilter =
        IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        registerReceiver(receiver, connectionStateFilter)
        }
        */

        /*
        注册蓝牙可检测性监听广播
        if (ActivityCompat.checkSelfPermission(
        this@MainActivity,BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED
        ) {
        // 权限请求
        ActivityCompat.requestPermissions(
        this@MainActivity, arrayOf(BLUETOOTH_ADVERTISE), REQUEST_ADVERTISE_BT_P)

        }else{
        // 蓝牙可检测性监听广播
        val discoverableFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        registerReceiver(receiver, discoverableFilter)
        }
        */
    }

    // 定义监听广播，
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action.toString()
            when (action) {
                // 获取蓝牙设备信息
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    (context as MainActivity).viewModel.add(
                        Device(
                            name = device?.name,
                            address = device?.address.toString()
                        )
                    )
                }
                // 监听蓝牙可检测性
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                    val state: Any? = intent.getParcelableExtra(BluetoothAdapter.EXTRA_SCAN_MODE)
                    when(state){
                        BluetoothAdapter.SCAN_MODE_NONE -> {
                            // 设备未处于可检测模式
                            // 启动蓝牙可检测性
                            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                            }
                            startActivityForResult(discoverableIntent,REQUEST_DISCOVERABLE_BT)
                        }
                        BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
                            // 设备处于可连接模式，但不可被发现
                            // 启动蓝牙可检测性
                            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60)
                            }
                            startActivityForResult(discoverableIntent,REQUEST_DISCOVERABLE_BT)

                        }
                        BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                            // 设备处于可连接和可发现模式
                            // 发现蓝牙设备监听广播
                            val deviceFoundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                            registerReceiver(this, deviceFoundFilter)
                        }
                        else -> {
                            val deviceFoundFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                            registerReceiver(this, deviceFoundFilter)
                        }
                    }
                }
                // 监听蓝牙连接转态
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state: Any? = intent.getParcelableExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE)
                    when(state){
                        BluetoothAdapter.STATE_DISCONNECTED -> {

                        }
                        BluetoothAdapter.STATE_CONNECTING-> {

                        }
                        BluetoothAdapter.STATE_CONNECTED -> {

                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            // 请求打开蓝牙
            REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    // 蓝牙打开成功
                }else{
                    // 蓝牙打开失败
                    Toast.makeText(this,"蓝牙打开失败！", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_DISCOVERABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    // 蓝牙可被检测到
                }else {
                    // 蓝牙可被检测性启动错误
                }
            }
        }
    }

    // 处理权限请求结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_PERMISSIONS_BT_P -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 蓝牙权限获取成功
                }else {
                    Toast.makeText(this,"获取蓝牙权限失败！", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val TAG ="THREAD_DEBUG"
    val NAME = "BLUETOOTH_APP"
    val MY_UUID : UUID = UUID.fromString("22DFC9DE-B21D-F7E5-C7FC-37425C9F917E")

    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket.
                // This call blocks until it succeeds or throws an exception.
                socket.connect()

            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

