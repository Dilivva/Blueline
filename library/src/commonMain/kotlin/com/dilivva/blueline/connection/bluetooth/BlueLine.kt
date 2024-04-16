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

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining a contract for interacting with Bluetooth devices,
 * specifically printers, across different platforms.
 * This interface provides a platform-agnostic way to manage Bluetooth connections,
 * printer discovery, and printing functionalities.
 * It abstracts away platform-specific details and offers a consistent API for various operations.
 */
interface BlueLine {

    /**
     * Provides a live stream of updates on the current connection state with the Bluetooth printer.
     * This method returns a [StateFlow] which emits updates whenever the connection state changes.
     * The [ConnectionState] object contains detailed information about:
     *  * Device name (if discovered)
     *  * Discovery status (discovered printer or not)
     *  * Print-ability of the discovered device
     *  * Connection status (connected or not)
     *  * Bluetooth readiness status (Bluetooth enabled or not)
     *  * Details of any Bluetooth connection errors (optional)
     *  * Printing status (currently printing or not)
     *  * Scanning status (actively searching for printers or not) (if applicable)
     *
     * @return A [StateFlow] emitting updates on the connection state.
     */
    fun connectionState(): StateFlow<ConnectionState>

    /**
     * Initializes the Bluetooth functionality on the device.
     * This method typically checks if Bluetooth is enabled and ready for use.
     * It's essential to call [init] before performing any other Bluetooth operations with the device.
     */
    fun init()

    /**
     * Initiates a scan for available Bluetooth printers in the vicinity.
     * This method starts a scan for discoverable Bluetooth printers. The scan has a timeout of 10 seconds by default (consider making this configurable if needed).
     * **Important:**
     *  * Call [init] before scanning to ensure Bluetooth is ready.
     *  * This method prioritizes the first discovered printer and might ignore others. Consider revising this behavior if you need to select a specific printer.
     *
     * @throws CancellationException if the coroutine is cancelled during the scan.
     */
    suspend fun scanForPrinters()

    /**
     * Attempts to establish a connection with the previously discovered Bluetooth printer.
     *
     * This method tries to connect to the printer that was found during the last [scanForPrinters] call.
     * It's important to ensure a successful [scanForPrinters] execution before calling [connect].
     */
    fun connect()

    /**
     * Disconnects from the currently connected Bluetooth printer.
     *
     * This method attempts to disconnect from the established connection with the printer.
     * If no connection is present, this method has no effect.
     */
    fun disconnect()

    /**
     * Sends print data to the connected Bluetooth printer.
     *
     * This method expects a byte array containing the data to be printed.
     * You might need to use data builders to generate the appropriate byte array format for printing.
     * @param data The byte array containing the print job data.
     */
    fun print(data: ByteArray)
}
