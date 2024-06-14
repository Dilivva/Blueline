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

/**
 * Represents different types of errors that can occur during the Bluetooth connection process.
 */
enum class ConnectionError {
    /**
     * Indicates that Bluetooth is currently disabled on the device.
     */
    BLUETOOTH_DISABLED,

    /**
     * Indicates that the application lacks the necessary permissions to access Bluetooth functionalities.
     */
    BLUETOOTH_PERMISSION,

    /**
     * Indicates that the device does not support Bluetooth functionality.
     */
    BLUETOOTH_NOT_SUPPORTED,


    BLUETOOTH_PRINT_ERROR,

    BLUETOOTH_PRINTER_DEVICE_NOT_FOUND
}


/**
 * Represents the current state of a printer connection process.
 *
 * This data class provides a structured way to hold information about the different stages
 * and details involved in connecting to a printer. It can be used to keep track of the
 * connection progress, communicate status to the UI, and provide context for error handling.
 *
 * @property deviceName A human-readable name representing the current device.
 * @property discoveredPrinter Indicates whether a printer has been discovered during the search process.
 * @property canPrint Indicates if the discovered printer is considered printable (might depend on additional factors like compatibility or supported features).
 * @property isConnected Indicates if a successful connection has been established with the printer.
 * @property isBluetoothReady Specific to Bluetooth connections, indicates if the Bluetooth functionality is ready for use (e.g., Bluetooth enabled on the device).
 * @property bluetoothConnectionError An optional `ConnectionError` object containing details about any errors encountered during Bluetooth connection attempts.
 * @property isPrinting Indicates if a print job is currently being sent to the printer.
 * @property isScanning Indicates if the system is actively searching for printers (relevant for discovery phase).
 */
data class ConnectionState(
    val deviceName: String = "Searching",
    val discoveredPrinter: Boolean = false,
    val canPrint: Boolean = false,
    val isConnected: Boolean = false,
    val isBluetoothReady: Boolean = false,
    val bluetoothConnectionError: ConnectionError? = null,
    val isPrinting: Boolean = false,
    val isScanning: Boolean = false,
    val isConnecting:Boolean = false
)
