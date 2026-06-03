package com.gymtracker.platform

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*

actual class ImageProcessor actual constructor(private val context: PlatformContext) {

    actual fun saveRawBytes(bytes: ByteArray, filename: String): String {
        val path = documentsPath(filename)
        bytes.toNSData().writeToFile(path, atomically = true)
        return path
    }

    actual fun saveFramedPhoto(
        bytes: ByteArray,
        panX: Float,
        panY: Float,
        userScale: Float,
        viewW: Float,
        viewH: Float,
        filename: String,
    ): String {
        val image = UIImage.imageWithData(bytes.toNSData()) ?: return ""
        val bmpW = image.size.useContents { width.toFloat() }
        val bmpH = image.size.useContents { height.toFloat() }

        val baseScale = maxOf(viewW / bmpW, viewH / bmpH)
        val totalScale = baseScale * userScale
        val cropW = (viewW / totalScale).coerceAtMost(bmpW)
        val cropH = (viewH / totalScale).coerceAtMost(bmpH)
        val left = (bmpW / 2f - viewW / (2f * totalScale) - panX / totalScale)
            .coerceIn(0f, (bmpW - cropW).coerceAtLeast(0f))
        val top = (bmpH / 2f - viewH / (2f * totalScale) - panY / totalScale)
            .coerceIn(0f, (bmpH - cropH).coerceAtLeast(0f))

        val path = documentsPath(filename)
        return cropAndSave(
            image,
            left.toDouble(), top.toDouble(), cropW.toDouble(), cropH.toDouble(),
            path, quality = 0.9,
        )
    }

    actual fun cropAndReplacePhoto(
        sourcePath: String,
        relLeft: Float,
        relTop: Float,
        relRight: Float,
        relBottom: Float,
    ): String {
        val image = UIImage.imageWithContentsOfFile(sourcePath) ?: return ""
        val bmpW = image.size.useContents { width.toDouble() }
        val bmpH = image.size.useContents { height.toDouble() }

        val x = relLeft.toDouble() * bmpW
        val y = relTop.toDouble() * bmpH
        val w = (relRight - relLeft).toDouble() * bmpW
        val h = (relBottom - relTop).toDouble() * bmpH

        val dir = (sourcePath as NSString).stringByDeletingLastPathComponent
        val newPath = "$dir/body_${NSUUID().UUIDString}.jpg"
        val result = cropAndSave(image, x, y, w, h, newPath, quality = 0.95)

        if (result.isNotEmpty()) {
            NSFileManager.defaultManager.removeItemAtPath(sourcePath, null)
        }
        return result
    }

    private fun cropAndSave(
        image: UIImage,
        x: Double, y: Double, w: Double, h: Double,
        destPath: String,
        quality: Double,
    ): String {
        val cgImage = image.CGImage ?: return ""
        val cropRect = CGRectMake(x, y, w, h)
        val croppedCG = CGImageCreateWithImageInRect(cgImage, cropRect) ?: return ""
        val cropped = UIImage.imageWithCGImage(croppedCG)
        CGImageRelease(croppedCG)
        val data = UIImageJPEGRepresentation(cropped, quality) ?: return ""
        return if (data.writeToFile(destPath, atomically = true)) destPath else ""
    }

    private fun documentsPath(filename: String): String {
        val dirs = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, expandTilde = true,
        )
        val dir = dirs.firstOrNull() as? String ?: return ""
        return "$dir/$filename"
    }

    private fun ByteArray.toNSData(): NSData = this.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), this.size.convert())
    }
}
