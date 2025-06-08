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


package com.dilivva.blueline.core.commands

/**
 * Sealed class representing various configuration options for printer output.
 *
 * This sealed class provides nested enums for defining different printer configuration aspects:
 *  * Alignment: Defines text alignment (center, left, right).
 *  * Font: Defines font styles (normal, wide, tall, various large sizes).
 *  * Color: Defines text color (black, red - potentially more can be added).
 *  * Style: Defines text styles (normal, bold, underline).
 */
sealed class Config {

    /**
     * Enum representing text alignment options for printing.
     *
     * Each enum value contains the corresponding byte array representing the escape sequence
     * to be sent to the printer for setting the alignment.
     */
    enum class Alignment(val alignBytes: ByteArray) {
        /** Center align the printed text. */
        CENTER(byteArrayOf(0x1B, 0x61, 0x01)),

        /** Left align the printed text. */
        LEFT(byteArrayOf(0x1B, 0x61, 0x00)),

        /** Right align the printed text. */
        RIGHT(byteArrayOf(0x1B, 0x61, 0x02))
    }

    /**
     * Enum representing font styles available for printing.
     *
     * Each enum value contains the corresponding byte array representing the escape sequence
     * to be sent to the printer for setting the font style.
     */
    enum class FontSize(val fontSizeBytes: ByteArray) {
        /** Normal font style. */
        NORMAL(byteArrayOf(0x1D, 0x21, 0x00)),

        /** Wide font style. */
        WIDE(byteArrayOf(0x1D, 0x21, 0x10)),

        /** Tall font style. */
        TALL(byteArrayOf(0x1D, 0x21, 0x01)),

        /** Large font styles (various sizes). */
        LARGE(byteArrayOf(0x1D, 0x21, 0x11)),
        LARGE_2(byteArrayOf(0x1D, 0x21, 0x22)),
        LARGE_3(byteArrayOf(0x1D, 0x21, 0x33)),
        LARGE_4(byteArrayOf(0x1D, 0x21, 0x44)),
        LARGE_5(byteArrayOf(0x1D, 0x21, 0x55)),
        LARGE_6(byteArrayOf(0x1D, 0x21, 0x66))
    }

    /**
     * Enum representing font styles available for printing.
     *
     * Each enum value contains the corresponding byte array representing the escape sequence
     * to be sent to the printer for setting the font style.
     */

    enum class Font(val fontBytes: ByteArray) {
        FONT_A(byteArrayOf(0x1B, 0x4D, 0x00)),
        FONT_B(byteArrayOf(0x1B, 0x4D, 0x01))
    }

    /**
     * Enum representing text color options for printing.
     *
     * Each enum value contains the corresponding byte array representing the escape sequence
     * to be sent to the printer for setting the text color.
     */
    enum class Color(val colorText: ByteArray) {
        /** Black text color. */
        BLACK(byteArrayOf(0x1B, 0x72, 0x00)),

        /** Red text color (more colors can potentially be added). */
        RED(byteArrayOf(0x1B, 0x72, 0x01))
    }

    /**
     * Enum representing text style options for printing.
     *
     * Each enum value contains the corresponding byte array representing the escape sequence
     * to be sent to the printer for setting the text style.
     */
    enum class Style(val style: ByteArray) {
        /** Normal text style. */
        NORMAL(byteArrayOf(0x1B, 0x45, 0x00)),

        /** Bold text style. */
        BOLD(byteArrayOf(0x1B, 0x45, 0x01)),

        /** Underlined text style. */
        UNDERLINE(byteArrayOf(0x1B, 0x2D, 0x01))
    }
}