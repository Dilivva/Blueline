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

import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBCentralManagerStatePoweredOff
import platform.CoreBluetooth.CBCentralManagerStatePoweredOn
import platform.CoreBluetooth.CBCentralManagerStateResetting
import platform.CoreBluetooth.CBCentralManagerStateUnauthorized
import platform.CoreBluetooth.CBCentralManagerStateUnknown
import platform.CoreBluetooth.CBCentralManagerStateUnsupported
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.darwin.NSObject

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
internal class ScanningManager(
    private val onDevice: (CBPeripheral) -> Unit,
    private val onConnection: (Boolean) -> Unit,
    private val onBluetoothReady: (BluetoothSettings) -> Unit
): NSObject(), CBCentralManagerDelegateProtocol {

    val UUID = CBUUID.UUIDWithString("18F0")

    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        val (isReady,message) = when(central.state){
            CBCentralManagerStatePoweredOn -> { Pair(true, null) }
            CBCentralManagerStatePoweredOff -> Pair(false, ConnectionError.BLUETOOTH_DISABLED)
            CBCentralManagerStateResetting -> Pair(false, ConnectionError.BLUETOOTH_DISABLED)
            CBCentralManagerStateUnauthorized -> Pair(false, ConnectionError.BLUETOOTH_PERMISSION)
            CBCentralManagerStateUnknown ->Pair(false, ConnectionError.BLUETOOTH_DISABLED)
            CBCentralManagerStateUnsupported -> Pair(false, ConnectionError.BLUETOOTH_NOT_SUPPORTED)
            else -> Pair(false, ConnectionError.BLUETOOTH_DISABLED)
        }
        onBluetoothReady(BluetoothSettings(isReady, message))
    }

    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber
    ) {
        central.stopScan()
        onDevice(didDiscoverPeripheral)
    }

    override fun centralManager(central: CBCentralManager, didConnectPeripheral: CBPeripheral) {
        onConnection(true)
        didConnectPeripheral.discoverServices(listOf(UUID))
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun centralManager(
        central: CBCentralManager,
        didDisconnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        onConnection(false)
    }

    @Suppress("CONFLICTING_OVERLOADS")
    override fun centralManager(
        central: CBCentralManager,
        didFailToConnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        onConnection(false)
    }




    data class BluetoothSettings(
        val isReady: Boolean = false,
        val error: ConnectionError? = null
    )

}