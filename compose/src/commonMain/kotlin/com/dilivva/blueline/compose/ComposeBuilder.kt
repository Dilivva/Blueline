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

package com.dilivva.blueline.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import com.dilivva.blueline.core.commands.Config
import com.dilivva.blueline.core.commands.PrintCommands
import com.dilivva.blueline.core.result.PrintDataResult
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.roundToInt


/**
 * Remembers a [ComposeBuilder] instance.
 *
 * This Composable function creates and remembers a [ComposeBuilder] with the specified parameters.
 * @param printerWidthInMm The width of the printer in millimeters.
 * @param useAsterisk A boolean flag indicating whether to use ESC * (asterisk) command for printing images.
 * @param alignment The alignment of the content (LEFT, CENTER, RIGHT).
 * @return A [ComposeBuilder] instance.
 */
@Composable
fun rememberComposeBuilder(
    printerWidthInMm: Float = 58f,
    useAsterisk: Boolean = false,
    alignment: Config.Alignment = Config.Alignment.LEFT
): ComposeBuilder {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(printerWidthInMm, useAsterisk, alignment, graphicsLayer) {
        ComposeBuilder(printerWidthInMm,useAsterisk, alignment, graphicsLayer)
    }
}

/**
 * Builder class for creating print data from Composable content.
 *
 * This class provides a way to render Composable UI into an [ImageBitmap] and then process
 * that bitmap into a byte array suitable for printing. It handles scaling the image to the
 * printer's width, converting it to monochrome, and formatting it according to ESC/POS commands.
 *
 * @property printerWidthInMm The width of the printer in millimeters. This is used to scale the output image.
 * @property useAsterisk A boolean flag indicating whether to use ESC * (asterisk) command for printing images.
 *                       If false, GS v 0 command is used.
 * @property alignment The alignment of the content (LEFT, CENTER, RIGHT).
 * @property graphicsLayer The [GraphicsLayer] used to capture the Composable content.
 */
class ComposeBuilder(
    private val printerWidthInMm: Float,
    private val useAsterisk: Boolean,
    private val alignment: Config.Alignment,
    private val graphicsLayer: GraphicsLayer
) {

    /**
     * Draws the provided Composable [content] into the [graphicsLayer].
     *
     * This function should be called within a Composable context. It sets up a [Box]
     * that uses [Modifier.drawWithContent] to record the drawing operations of the [content]
     * into the [graphicsLayer].
     *
     * @param content The Composable function that defines the UI to be printed.
     */
    @Composable
    fun drawContents(content: @Composable () -> Unit) {
        Box(
            modifier = Modifier
                .drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
        ) {
            content()
        }
    }

    /**
     * Creates the print data from the content drawn via [drawContents].
     *
     * This function processes the [ImageBitmap] captured by the [graphicsLayer],
     * scales it, converts it to monochrome, and generates the byte array for printing.
     * @return A [PrintDataResult] containing the byte array to be sent to the printer and a byte array representing a preview of the image.
     */
    suspend fun create(): PrintDataResult {
        val bitmap = graphicsLayer.toImageBitmap()
        val (bytesToPrint, preview) = process(bitmap)
        return PrintDataResult(bytesToPrint, preview.toByteArray())
    }

    private fun process(bitmap: ImageBitmap): Pair<ByteArray, ImageBitmap> {
        val scaled = bitmap.scaleToWidthCompose(mmToDots(printerWidthInMm))
        val pixelMap = scaled.toPixelMap()
        val escImage = bitmapToBytes(pixelMap)
        val printData = alignment.alignBytes + escImage.join()
        return printData to scaled
    }

    private fun bitmapToBytes(pixelMap: PixelMap): List<ByteArray> {
        val monochromeImage = processBitmapToBytes(pixelMap)
        return if (useAsterisk){
            processToEscAsterisk(monochromeImage)
        }else{
            processToGsv(monochromeImage)
        }
    }
    private fun processBitmapToBytes(pixelMap: PixelMap): ByteArray {
        val w = pixelMap.width
        val h = pixelMap.height
        val bytesPerLine = ceil(w / 8f).toInt()
        val imageBytes = initCommand(bytesPerLine, h)
        var writeIndex = 8

        var greyscaleCoeffStart = 0
        val gradientStep = 6
        val colorLevelStep = 765.0 / (15 * gradientStep + gradientStep - 1)

        for (y in 0 until h) {
            var coeff = greyscaleCoeffStart
            val lineOffset = y % gradientStep
            var x = 0
            while (x < w) {
                var b = 0
                for (k in 0 until 8) {
                    val px = x + k
                    if (px < w) {
                        val c = pixelMap.get(px, y)
                        val r = (c.red   * 255).roundToInt()
                        val g = (c.green * 255).roundToInt()
                        val bld= (c.blue  * 255).roundToInt()
                        val threshold = (coeff * gradientStep + lineOffset) * colorLevelStep
                        if (r + g + bld < threshold || r < 160 || g < 160 || bld < 160) {
                            b = b or (1 shl (7 - k))
                        }
                        coeff += 5
                        if (coeff > 15) coeff -= 16
                    }
                }

                imageBytes[writeIndex++] = b.toByte()
                x += 8
            }
            greyscaleCoeffStart = (greyscaleCoeffStart + 2) % 16
        }
        return imageBytes
    }

    private fun initCommand(bytesByLine: Int, bitmapHeight: Int): ByteArray {
        val xH = bytesByLine / 256
        val xL = bytesByLine - xH * 256
        val yH = bitmapHeight / 256
        val yL = bitmapHeight - yH * 256
        val imageBytes = ByteArray(8 + bytesByLine * bitmapHeight)
        imageBytes[4] = xL.toByte()
        imageBytes[5] = xH.toByte()
        imageBytes[6] = yL.toByte()
        imageBytes[7] = yH.toByte()
        return imageBytes
    }

    private fun processToEscAsterisk(byteArray: ByteArray): List<ByteArray>{
        val xL = byteArray[4].toInt() and 0xFF
        val xH = byteArray[5].toInt() and 0xFF
        val yL = byteArray[6].toInt() and 0xFF
        val yH = byteArray[7].toInt() and 0xFF
        val bytesByLine = xH * 256 + xL
        val dotsByLine = bytesByLine * 8
        val nH = dotsByLine / 256
        val nL = dotsByLine % 256
        val imageHeight = yH * 256 + yL
        val imageLineHeightCount = ceil(imageHeight.toDouble() / 24.0).toInt()
        val imageBytesSize = 6 + bytesByLine * 24

        val returnedBytes = arrayOfNulls<ByteArray>(imageLineHeightCount + 2)
        returnedBytes[0] = PrintCommands.SET_LINE_SPACING_24
        for (i in 0 until imageLineHeightCount) {
            val pxBaseRow = i * 24
            val imageBytes = ByteArray(imageBytesSize)
            imageBytes[0] = 0x1B
            imageBytes[1] = 0x2A
            imageBytes[2] = 0x21
            imageBytes[3] = nL.toByte()
            imageBytes[4] = nH.toByte()
            for (j in 5 until imageBytes.size) {
                val imgByte = j - 5
                val byteRow = imgByte % 3
                val pxColumn = imgByte / 3
                val bitColumn = 1 shl 7 - pxColumn % 8
                val pxRow = pxBaseRow + byteRow * 8
                for (k in 0..7) {
                    val indexBytes = bytesByLine * (pxRow + k) + pxColumn / 8 + 8
                    if (indexBytes >= byteArray.size) {
                        break
                    }
                    val isBlack = byteArray[indexBytes].toInt() and bitColumn == bitColumn
                    if (isBlack) {
                        imageBytes[j] = (imageBytes[j].toInt() or (1 shl 7 - k)).toByte()
                    }
                }
            }
            imageBytes[imageBytes.size - 1] = PrintCommands.NEW_LINE
            returnedBytes[i + 1] = imageBytes
        }
        return returnedBytes.filterNotNull()
    }
    private fun processToGsv(byteArray: ByteArray): List<ByteArray>{
        byteArray[0] = 0x1D
        byteArray[1] = 0x76
        byteArray[2] = 0x30
        byteArray[3] = 0x00
        return listOf(byteArray)
    }


    private fun mmToPx(mmSize: Float): Int {
        return round(mmSize * 203f / 25.4f).toInt()
    }
    private fun getPrinterWidthPx() = mmToPx(58f)

    private fun mmToDots(mm: Float, dpi: Float = 203f): Int =
        ((mm / 25.4f) * dpi).roundToInt()

    private fun mmToBytesPerLine(mm: Float, dpi: Float = 203f): Int {
        val dots = mmToDots(mm, dpi)
        return ceil(dots / 8f).toInt()
    }
}