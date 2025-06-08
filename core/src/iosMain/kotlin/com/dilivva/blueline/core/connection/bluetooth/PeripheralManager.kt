package com.dilivva.blueline.core.connection.bluetooth

import platform.CoreBluetooth.CBCharacteristic
import platform.CoreBluetooth.CBPeripheral
import platform.CoreBluetooth.CBPeripheralDelegateProtocol
import platform.CoreBluetooth.CBService
import platform.CoreBluetooth.CBUUID
import platform.Foundation.NSError
import platform.darwin.NSObject

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
internal class PeripheralManager(
    private val onCharacter: (CBCharacteristic) -> Unit,
    private val onWrite: (Boolean) -> Unit
): NSObject(), CBPeripheralDelegateProtocol {

    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverCharacteristicsForService: CBService,
        error: NSError?
    ) {
        val character = didDiscoverCharacteristicsForService.characteristics?.find {
            (it as CBCharacteristic).UUID == CBUUID.UUIDWithString("2AF1")
        } as? CBCharacteristic
        character?.let {
            onCharacter(it)
        }
    }

    override fun peripheral(peripheral: CBPeripheral, didDiscoverServices: NSError?) {
        val service = peripheral.services?.find { (it as CBService).UUID == CBUUID.UUIDWithString("18F0") } as? CBService
        if (service != null) {
            peripheral.discoverCharacteristics(null, service)
        }
    }

    override fun peripheral(
        peripheral: CBPeripheral,
        didWriteValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        onWrite(error == null)
    }



}