package com.dilivva.blueline.compose

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection

/**
 * Converts [ImageBitmap] to image and returns its bytes.
 * */
internal expect fun ImageBitmap.toByteArray() : ByteArray

internal fun ImageBitmap.scaleToWidthCompose(scaleWidth: Int): ImageBitmap {
    if (scaleWidth <= 0 || width == scaleWidth) return this
    val scale = scaleWidth.toFloat() / width
    val targetHeight = maxOf((height * scale).toInt(), 1)
    val output = ImageBitmap(scaleWidth, targetHeight)
    val old = this.toByteArray().decodeToImageBitmap()

    Canvas(output).let { canvas ->
        CanvasDrawScope().draw(
            canvas = canvas,
            size   = Size(scaleWidth.toFloat(), targetHeight.toFloat()),
            density = Density(1f, 1f),
            layoutDirection = LayoutDirection.Ltr
        ) {
            drawImage(
                image     = old,
                srcOffset = IntOffset.Zero,
                srcSize   = IntSize(width, height),
                dstOffset = IntOffset.Zero,
                dstSize   = IntSize(scaleWidth, targetHeight),
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            )
        }
    }

    return output
}