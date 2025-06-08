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


package com.dilivva.blueline.core.commands

internal class PrinterHelper(
    private val onNext: (ByteArray) -> Unit,
    private val onDone: () -> Unit
) {
    var mtu = 20

    private var currentBytes = listOf<ByteArray>()
    private var currentIndex = 0

    fun begin(data: ByteArray){
        reset()
        val initData = PrintCommands.RESET_CONFIGURATION + data
        currentBytes = splitByteArray(initData, mtu)
        sendNextBytes()
    }

    fun sendNextBytes(){
        if (currentIndex < currentBytes.size){
            onNext(currentBytes[currentIndex])
            currentIndex++
        }
        if (currentIndex == currentBytes.size){
            onDone()
        }
    }

    private fun reset(){
        currentIndex = 0
        currentBytes = emptyList()
    }

    private fun splitByteArray(data: ByteArray, chunkSize: Int): List<ByteArray> {
        val chunks = mutableListOf<ByteArray>()
        var startIndex = 0
        while (startIndex < data.size) {
            val endIndex = minOf(startIndex + chunkSize, data.size)
            val chunk = data.sliceArray(startIndex until endIndex)
            chunks.add(chunk)
            startIndex += chunkSize
        }
        return chunks
    }
}