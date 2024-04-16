/*
 * Copyright (C) 2024, Send24.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT
 * SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */


package com.dilivva.blueline.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import com.dilivva.blueline.commands.PrinterHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@SuppressLint("MissingPermission")
internal object AndroidBluetoothConnection: BlueLine {

    private val stateFlow = MutableStateFlow(ConnectionState())
    private val bluetoothManager: BluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    //Service UUID is 16bit e.g FF00, to scan we'll have to use 0000xxxx-0000-1000-8000-00805F9B34FB
    private val printerUUID = ParcelUuid.fromString("000018F0-0000-1000-8000-00805F9B34FB")
    private val characterUuid = UUID.fromString("00002AF1-0000-1000-8000-00805F9B34FB")

    private var bluetoothDevice: BluetoothDevice? = null
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null
    private var characteristic: BluetoothGattCharacteristic? = null

    /**
     * using Write type with NO RESPONSE cannot send large data
     * use, default [BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT] to send larger writes
     */
    private val printerHelper = PrinterHelper(
        onNext = { data ->
            characteristic?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bluetoothGatt?.writeCharacteristic(it, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                } else {
                    it.setValue(data)
                    it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    bluetoothGatt?.writeCharacteristic(it)
                }
            }
        },
        onDone = {
            stateFlow.update { it.copy(isPrinting = false) }
        }
    )

    private val scanCallback = object: ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val device = result?.device
            if (device != null){
                bluetoothLeScanner?.stopScan(this)
                bluetoothDevice = device
                stateFlow.update { state -> state.copy(deviceName = device.name.orEmpty(), discoveredPrinter = true, isScanning = false) }
            }
        }
    }
    private val gattCallback = object: BluetoothGattCallback(){

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                stateFlow.update { state -> state.copy(isConnected = true) }
                gatt?.discoverServices()
            }else{
                stateFlow.update { state -> state.copy(isConnected = false) }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(printerUUID.uuid)
                val characteristics = service?.characteristics
                val character = characteristics?.find { it.uuid == characterUuid }
                characteristic = character
                if (characteristic != null){
                    stateFlow.update { state -> state.copy(canPrint = true) }
                    gatt?.requestMtu(512)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                printerHelper.sendNextBytes()
            }else{
                stateFlow.update { it.copy(isPrinting = false, bluetoothConnectionError = ConnectionError.BLUETOOTH_PRINT_ERROR) }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            printerHelper.mtu = minOf(mtu * 2, 512)
        }


    }


    init {
        init()
    }

    override fun init() {
        if (bluetoothAdapter == null){
            stateFlow.update { state ->
                state.copy(isBluetoothReady = false, bluetoothConnectionError = ConnectionError.BLUETOOTH_NOT_SUPPORTED)
            }
            return
        }
        if (!bluetoothAdapter.isEnabled){
            stateFlow.update { state ->
                state.copy(isBluetoothReady = false, bluetoothConnectionError = ConnectionError.BLUETOOTH_DISABLED)
            }
            return
        }
        if (bluetoothAdapter.isEnabled){
            stateFlow.update { state ->
                state.copy(isBluetoothReady = true, bluetoothConnectionError = null)
            }
        }

    }

    override fun connectionState(): StateFlow<ConnectionState> {
        return stateFlow.asStateFlow()
    }

    override suspend fun scanForPrinters() {
        if (!stateFlow.value.isBluetoothReady){
            return
        }
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        val pairedPrinter = pairedDevices?.find { it.uuids.contains(printerUUID) }
        if (pairedPrinter != null){
            bluetoothDevice = pairedPrinter
            stateFlow.update { state -> state.copy(deviceName = pairedPrinter.name.orEmpty(), discoveredPrinter = true) }
            return
        }
        scanLeDevice()

    }

    override fun connect() {
        val state = stateFlow.value
        if (!state.isBluetoothReady || state.isConnected){
            return
        }
        if (bluetoothGatt != null){
            bluetoothGatt?.connect()
            return
        }
        bluetoothGatt = bluetoothDevice?.connectGatt(applicationContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    override fun disconnect() {
        val state = stateFlow.value
        if (!state.isBluetoothReady || !state.isConnected || bluetoothGatt == null) return
        bluetoothGatt?.disconnect()
    }

    override  fun print(data: ByteArray) {
        printerHelper.begin(data)
        stateFlow.update { it.copy(isPrinting = true) }
    }

    private suspend fun scanLeDevice() {
        if (!applicationContext.isLocationEnabled()){
            stateFlow.update { it.copy(bluetoothConnectionError = ConnectionError.BLUETOOTH_PERMISSION, isScanning = false) }
            return
        }
        if (stateFlow.value.isScanning){
            bluetoothLeScanner?.stopScan(scanCallback)
            stateFlow.update { it.copy(isScanning = false) }
        }
        stateFlow.update { it.copy(bluetoothConnectionError = null, isScanning = true) }
        bluetoothLeScanner?.startScan(
            listOf(ScanFilter.Builder().setServiceUuid(printerUUID).build()),
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build(),
            scanCallback
        )
        delay(10.seconds)
        if (!stateFlow.value.discoveredPrinter){
            stateFlow.update { it.copy(bluetoothConnectionError = ConnectionError.BLUETOOTH_PRINTER_DEVICE_NOT_FOUND, isScanning = false) }
        }
        bluetoothLeScanner?.stopScan(scanCallback)
    }

}