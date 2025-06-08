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

expect fun String.encodeUsingEncoder(): ByteArray

class TextBuilder{

    private var textData = byteArrayOf()

    /**
     * Appends formatted text to the print data buffer with the specified configuration.
     *
     * This function takes a string containing the text data and several optional configuration parameters:
     *  * `alignment`: Defines the text alignment (defaults to left alignment).
     *  * `font`: Defines the font style (defaults to normal font).
     *  * `style`: Defines the text style (defaults to normal style).
     *  * `color`: Defines the text color (defaults to black).
     *
     * @param data The string containing the text to be printed.
     * @param alignment The desired text alignment (optional, defaults to left).
     * @param font The desired font style (optional, defaults to [Config.Font.FONT_A]).
     * @param fontSize The desired font style (optional, defaults to normal).
     * @param style The desired text style (optional, defaults to normal).
     * @param color The desired text color (optional, defaults to black).
     */
    fun styledText(
        data: String,
        alignment: Config.Alignment = Config.Alignment.LEFT,
        font: Config.Font = Config.Font.FONT_A,
        fontSize: Config.FontSize = Config.FontSize.NORMAL,
        style: Config.Style = Config.Style.NORMAL,
        color: Config.Color = Config.Color.BLACK
    ){
        resetConfig()
        textData += alignment.alignBytes
        textData += font.fontBytes
        textData += fontSize.fontSizeBytes
        textData += style.style
        textData += color.colorText

        val textsData = data.encodeUsingEncoder()
        textData += textsData
    }

    /**
     * Appends plain text to the print data buffer with left alignment.
     *
     * This function is a simplified version of `styledText` that only takes the text data as a parameter.
     * It uses left alignment (`Config.Alignment.LEFT`) as the default and sets all other formatting options
     * (font, style, color) to their default values.
     *
     * @param data The string containing the text to be printed (plain text with default formatting).
     */
    fun text(data: String){
        resetConfig()
        textData += Config.Alignment.LEFT.alignBytes
        textData += data.encodeUsingEncoder()
    }

    /**
     * Inserts line breaks into the print data buffer.
     * @param times The number of line breaks to insert (defaults to 1).
     */
    fun textNewLine(times: Int = 1){
        repeat(times){
            textData += PrintCommands.NEW_LINE
        }
    }

    internal fun build(): ByteArray{
        return textData
    }

    private fun resetConfig(){
        textData += Config.Alignment.LEFT.alignBytes
        textData += Config.FontSize.NORMAL.fontSizeBytes
        textData += Config.Style.NORMAL.style
        textData += Config.Color.BLACK.colorText
    }


}