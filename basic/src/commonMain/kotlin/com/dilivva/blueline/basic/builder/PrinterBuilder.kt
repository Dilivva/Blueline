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


package com.dilivva.blueline.basic.builder

import com.dilivva.blueline.core.commands.PrintCommands
import com.dilivva.blueline.core.commands.PrinterEncoding
import com.dilivva.blueline.core.result.PrintDataResult


/**
 * Builds a byte array containing the print data based on the provided configuration.
 * @param data A lambda expression that configures the `PrinterBuilder` for defining the print data.
 * @return [PrintDataResult] A pair containing the primary byte array with the print data and an optional secondary byte array
 *         with image data (if applicable).
 */

fun buildPrintData(data: PrinterBuilder.() -> Unit): PrintDataResult {
    return PrinterBuilder().apply(data).build()
}

class PrinterBuilder{

    private var printData = byteArrayOf()
    private val printerEncoding = PrinterEncoding()
    private var printPreview: ByteArray? = null


    /**
     * Appends formatted text to the print data buffer.
     * @param text A lambda expression that configures the [TextBuilder] for defining the formatted text.
     */
    fun appendText(text: TextBuilder.() -> Unit){
        if (printData.isNotEmpty()) resetPrinter()
        printData += printerEncoding.getCommand()
        val data = TextBuilder().apply(text).build()
        printData += data
    }
    /**
     * Appends an image to the print data buffer.
     * @param image A lambda expression that configures the `ImageBuilder` for defining the image to be printed.
     */
    fun appendImage(image: ImageBuilder.() -> Unit){
        if (printData.isNotEmpty()) resetPrinter()
        val imageBuilder = ImageBuilder().apply(image)
        printData += imageBuilder.build()
        printPreview = imageBuilder.imagePreview
    }

    /**
     * Inserts line breaks into the print data buffer.
     * This function adds line breaks to the buffer. It takes an optional [times] parameter
     * which defaults to 1.
     * @param times The number of line breaks to insert (defaults to 1).
     */
    fun newLine(times: Int = 1){
        repeat(times){
            printData += PrintCommands.NEW_LINE
        }
    }

    private fun resetPrinter(){
        printData += PrintCommands.RESET_CONFIGURATION
    }

    internal fun build(): PrintDataResult {
        newLine(5)
        return PrintDataResult(printData, printPreview)
    }

}

