/*
 * Copyright (C) 2025, Send24.
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

package com.dilivva.blueline.core.connection.bluetooth

import com.dilivva.blueline.core.commands.PrinterHelper
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBCharacteristicWriteWithResponse
import platform.CoreBluetooth.CBPeripheral
import platform.Foundation.NSData
import platform.Foundation.NSUUID
import platform.Foundation.create
import kotlin.time.Duration.Companion.seconds

internal object IosBluetoothConnection: BlueLine {

    private val printerHelper = PrinterHelper(
        onNext = { data ->
            val nsData = data.toData()
            if (peripheral != null && characteristic != null) {
                peripheral?.writeValue(nsData, characteristic ?: CBCharacteristic(), CBCharacteristicWriteWithResponse)
            }
        },
        onDone = {
            stateFlow.update { it.copy(isPrinting = false) }
        }
    )

    private val discoveredPeripheralsForListScan = mutableMapOf<String, SimpleBluetoothDevice>()

    private val delegate = ScanningManager(
        onDevice = { device ->
            discoveredPeripheralsForListScan[device.identifier.UUIDString] = SimpleBluetoothDevice(
                name = device.name ?: "Unknown device",
                address = device.identifier.UUIDString,
                bluetoothDevice = device,
            )
            stateFlow.update { state -> state.copy(discoveredDevices = discoveredPeripheralsForListScan, discoveredPrinter = true, isScanning = false) }
        },
        onConnection = {
            stateFlow.update { state -> state.copy(isConnected = it, isConnecting = false, bluetoothConnectionError = if (it) null else ConnectionError.BLUETOOTH_PRINTER_DEVICE_NOT_FOUND) }
        },
        onBluetoothReady = {
            stateFlow.update { state ->
                state.copy(isBluetoothReady = it.isReady, bluetoothConnectionError = it.error)
            }
        }
    )

    private val peripheralManager = PeripheralManager(
        onCharacter = { character ->
            characteristic = character
            stateFlow.update { state -> state.copy(canPrint = true) }
            val mtu = peripheral?.maximumWriteValueLengthForType(CBCharacteristicWriteWithResponse)?.toInt()
            printerHelper.mtu = mtu ?: 20
        },
        onWrite = { isPrintedSuccessfully ->
            if (isPrintedSuccessfully) {
                printerHelper.sendNextBytes()
            }else{
                stateFlow.update { it.copy(isPrinting = false, bluetoothConnectionError = ConnectionError.BLUETOOTH_PRINT_ERROR) }
            }
        }
    )

    private lateinit var centralManager: CBCentralManager
    private var peripheral: CBPeripheral? = null
    private var characteristic: CBCharacteristic? = null
    private val stateFlow = MutableStateFlow(ConnectionState())


    init {
        init()
    }


    override fun init() {
        centralManager = CBCentralManager(delegate = delegate, queue = null)
    }

    override suspend fun scanForPrinters(){
        val isReady = stateFlow.value.isBluetoothReady
        if (!isReady) return

        if (stateFlow.value.isScanning){
            centralManager.stopScan()
            stateFlow.update { it.copy(isScanning = false) }
        }
        discoveredPeripheralsForListScan.clear()

        stateFlow.update { it.copy(bluetoothConnectionError = null, isScanning = true) }
        centralManager.scanForPeripheralsWithServices(serviceUUIDs = listOf(delegate.UUID), options = null)

        delay(5.seconds)
        if (!stateFlow.value.discoveredPrinter){
            stateFlow.update { it.copy(bluetoothConnectionError = ConnectionError.BLUETOOTH_PRINTER_DEVICE_NOT_FOUND, isScanning = false) }
        }
        centralManager.stopScan()
    }

    override fun connectionState(): StateFlow<ConnectionState>{
       return stateFlow.asStateFlow()
    }

    override fun connect(deviceAddress: String) {
        if (!stateFlow.value.isBluetoothReady) {
            stateFlow.update { it.copy(isConnecting = false, bluetoothConnectionError = ConnectionError.BLUETOOTH_DISABLED) }
            return
        }
        if (stateFlow.value.isConnected && stateFlow.value.connectedDevice?.bluetoothDevice?.identifier?.UUIDString == deviceAddress) {
            stateFlow.update{ it.copy(isConnecting = false)} // Already connected to this one
            return
        }
        if (stateFlow.value.isConnecting) return // Already trying to connect

        stateFlow.update { it.copy(isConnecting = true, bluetoothConnectionError = null) }

        val peripheralToConnect = discoveredPeripheralsForListScan[deviceAddress]?.bluetoothDevice // Try cache from list scan
            ?: centralManager.retrievePeripheralsWithIdentifiers(listOf(NSUUID(uUIDString = deviceAddress))).firstOrNull() as? CBPeripheral

        if (peripheralToConnect == null) {
            stateFlow.update { it.copy(isConnecting = false, bluetoothConnectionError = ConnectionError.BLUETOOTH_PRINTER_DEVICE_NOT_FOUND) }
            return
        }

        // If the original connection is active to a DIFFERENT device, disconnect it.
        if (this.peripheral != null && this.peripheral?.identifier != peripheralToConnect.identifier) {
            centralManager.cancelPeripheralConnection(this.peripheral!!)
        }

        peripheralToConnect.delegate = peripheralManager
        centralManager.connectPeripheral(peripheralToConnect, null)
        peripheral = peripheralToConnect
        stateFlow.update {
            it.copy(connectedDevice = SimpleBluetoothDevice(
                name = peripheralToConnect.name ?: "Unknown device",
                address = peripheralToConnect.identifier.UUIDString,
                bluetoothDevice = peripheralToConnect
            ))
        }
    }

    override fun disconnect() {
        val state = stateFlow.value
        if (!state.isBluetoothReady || !state.isConnected || peripheral == null) return
        peripheral?.let {
            centralManager.cancelPeripheralConnection(it)
        }
        stateFlow.update { it.copy(connectedDevice = null) }
    }

    override fun print(data: ByteArray) {
        printerHelper.begin(data)
        stateFlow.update { it.copy(isPrinting = true) }
    }

    @Suppress("OPT_IN_USAGE")
    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toData(): NSData = memScoped {
        NSData.create(bytes = allocArrayOf(this@toData), length = this@toData.size.toULong())
    }


}
