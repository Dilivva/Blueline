package com.dilivva.blueline.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.toByteArray() : ByteArray{
    val data = Image
        .makeFromBitmap(asSkiaBitmap())
        .encodeToData(EncodedImageFormat.PNG) ?: error("Error while encoding")
    return data.bytes
}