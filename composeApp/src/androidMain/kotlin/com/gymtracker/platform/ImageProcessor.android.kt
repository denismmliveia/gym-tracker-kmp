package com.gymtracker.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.util.UUID

actual class ImageProcessor actual constructor(private val context: PlatformContext) {

    actual fun saveRawBytes(bytes: ByteArray, filename: String): String {
        val dest = File(context.androidContext.filesDir, filename)
        dest.outputStream().use { it.write(bytes) }
        return dest.absolutePath
    }

    actual fun saveFramedPhoto(
        bytes: ByteArray,
        panX: Float, panY: Float, userScale: Float,
        viewW: Float, viewH: Float,
        filename: String
    ): String {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return ""
        val bmpW = bitmap.width.toFloat()
        val bmpH = bitmap.height.toFloat()

        val baseScale = maxOf(viewW / bmpW, viewH / bmpH)
        val totalScale = baseScale * userScale

        val cropW = (viewW / totalScale).coerceAtMost(bmpW)
        val cropH = (viewH / totalScale).coerceAtMost(bmpH)

        val left = (bmpW / 2f - viewW / (2f * totalScale) - panX / totalScale)
            .coerceIn(0f, (bmpW - cropW).coerceAtLeast(0f))
        val top = (bmpH / 2f - viewH / (2f * totalScale) - panY / totalScale)
            .coerceIn(0f, (bmpH - cropH).coerceAtLeast(0f))

        val cropped = Bitmap.createBitmap(
            bitmap,
            left.toInt(), top.toInt(),
            cropW.toInt().coerceAtMost(bitmap.width - left.toInt()),
            cropH.toInt().coerceAtMost(bitmap.height - top.toInt())
        )
        bitmap.recycle()

        val dest = File(context.androidContext.filesDir, filename)
        dest.outputStream().use { cropped.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        cropped.recycle()
        return dest.absolutePath
    }

    actual fun cropAndReplacePhoto(
        sourcePath: String,
        relLeft: Float, relTop: Float, relRight: Float, relBottom: Float
    ): String {
        val original = BitmapFactory.decodeFile(sourcePath) ?: return ""
        val x = (relLeft * original.width).toInt().coerceIn(0, original.width - 1)
        val y = (relTop * original.height).toInt().coerceIn(0, original.height - 1)
        val w = ((relRight - relLeft) * original.width).toInt().coerceIn(1, original.width - x)
        val h = ((relBottom - relTop) * original.height).toInt().coerceIn(1, original.height - y)

        val cropped = Bitmap.createBitmap(original, x, y, w, h)
        original.recycle()

        val oldFile = File(sourcePath)
        val newFile = File(oldFile.parent, "body_${UUID.randomUUID()}.jpg")
        val ok = runCatching {
            newFile.outputStream().use { cropped.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            true
        }.getOrDefault(false)
        cropped.recycle()

        return if (ok) {
            oldFile.delete()
            newFile.absolutePath
        } else {
            newFile.delete()
            ""
        }
    }
}
