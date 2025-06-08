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

package com.dilivva.blueline.basic.builder

import com.dilivva.blueline.core.commands.Config
import com.dilivva.blueline.core.commands.PrintCommands
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.resized
import korlibs.image.color.Colors
import korlibs.image.format.PNG
import korlibs.io.util.join
import korlibs.math.geom.Anchor
import korlibs.math.geom.ScaleMode
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.sqrt


class ImageBuilder {

    var useAsterisk = false
    var width: Int? = null
    var height: Int? = null
    var fillWidth = false
    var alignment = Config.Alignment.LEFT

    var imageBytes = byteArrayOf()

    var imagePreview: ByteArray? = null

    fun build(): ByteArray {
        return image(imageBytes)
    }

    private fun image(image: ByteArray): ByteArray{
        val decodedImage = PNG.decode(image)
        val scaled = scaleImage(decodedImage)
        val converted = convertToMonochrome(scaled)

        imagePreview = PNG.encode(converted)
        val escImage = bitmapToBytes(converted)
        return alignment.alignBytes + escImage.join()
    }
    private fun scaleImage(bitmap: Bitmap): Bitmap{
        if (height == null && width == null && !fillWidth){
            return bitmap
        }
        if (fillWidth){
            return bitmap.resized(getPrinterWidthPx(), bitmap.height, ScaleMode.SHOW_ALL, Anchor.CENTER, native = true)
        }
        val newWidth = width ?: bitmap.width
        val newHeight = height ?: bitmap.height

        return bitmap.resized(newWidth, newHeight, ScaleMode.SHOW_ALL, Anchor.CENTER, native = true)
    }

    private fun bitmapToBytes(bitmap: Bitmap): List<ByteArray> {
        val monochromeImage = processBitmapToBytes(bitmap)
        return if (useAsterisk){
            processToEscAsterisk(monochromeImage)
        }else{
            processToGsv(monochromeImage)
        }
    }

    private fun convertToMonochrome(colorBitmap: Bitmap): Bitmap32 {
        val width = colorBitmap.width
        val height = colorBitmap.height
        val monoBitmap = colorBitmap.toBMP32IfRequired()

        val threshold = getThreshold(monoBitmap)
        val invert = threshold < 0.1

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = colorBitmap.getInt(x, y)
                val brightness = getBrightnessApprox(color)
                val monoColor = if (brightness > threshold){
                    if (invert) Colors.BLACK else Colors.WHITE
                } else {
                    if (invert) Colors.WHITE else Colors.BLACK
                }
                monoBitmap.setRgba(x,y, if (brightness.equals(0.0)) Colors.WHITE else monoColor)
            }
        }
        return monoBitmap
    }
    private fun getThreshold(bitmap: Bitmap32): Double{
        val width = bitmap.width
        val height = bitmap.height
        val brightnesses = mutableListOf<Double>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = bitmap.getInt(x, y)
                val brightness = getBrightnessApprox(color)
                brightnesses.add(brightness)
            }
        }
        return brightnesses.average()
    }

    private fun getBrightnessApprox(color: Int): Double {
        val alpha = (color shr 24 and 0xFF) / 255.0
        val red = (color shr 16 and 0xFF)
        val green = (color shr 8 and 0xFF)
        val blue = (color and 0xFF)
        val avgSquared = (red * red + green * green + blue * blue) / (3.0 * 255.0 * 255.0)
        return alpha * sqrt(avgSquared)
    }

    private fun processBitmapToBytes(bitmap: Bitmap): ByteArray {
        val bitmapWidth: Int = bitmap.width
        val bitmapHeight: Int = bitmap.height
        val bytesByLine: Int = ceil((bitmapWidth.toFloat() / 8f).toDouble()).toInt()
        val imageBytes: ByteArray = initCommand(bytesByLine, bitmapHeight)
        var i = 8
        var greyscaleCoefficientInit = 0
        val gradientStep = 6
        val colorLevelStep = 765.0 / (15 * gradientStep + gradientStep - 1)
        for (posY in 0 until bitmapHeight) {
            var greyscaleCoefficient = greyscaleCoefficientInit
            val greyscaleLine = posY % gradientStep
            var j = 0
            while (j < bitmapWidth) {
                var b = 0
                for (k in 0..7) {
                    val posX = j + k
                    if (posX < bitmapWidth) {
                        val color: Int = bitmap.getInt(posX, posY)
                        val red = color shr 16 and 255
                        val green = color shr 8 and 255
                        val blue = color and 255
                        if (red + green + blue < (greyscaleCoefficient * gradientStep + greyscaleLine) * colorLevelStep || (red < 160 || green < 160 || blue < 160)) {
                            b = b or (1 shl 7 - k)
                        }
                        greyscaleCoefficient += 5
                        if (greyscaleCoefficient > 15) {
                            greyscaleCoefficient -= 16
                        }
                    }
                }
                imageBytes[i++] = b.toByte()
                j += 8
            }
            greyscaleCoefficientInit += 2
            if (greyscaleCoefficientInit > 15) {
                greyscaleCoefficientInit = 0
            }
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
        //returnedBytes[returnedBytes.size - 1] = PrintCommands.SET_LINE_SPACING_30
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
    private fun getPrinterWidthPx() = mmToPx(48f)


}