package com.dilivva.blueline.compose

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.toByteArray() : ByteArray{
    return ByteArrayOutputStream().use {
        asAndroidBitmap()
        asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        it.toByteArray()
    }
}