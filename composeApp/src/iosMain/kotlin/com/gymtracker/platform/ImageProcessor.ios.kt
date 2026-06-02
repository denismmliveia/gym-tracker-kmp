package com.gymtracker.platform

actual class ImageProcessor actual constructor(private val context: PlatformContext) {

    actual fun saveRawBytes(bytes: ByteArray, filename: String): String = ""

    actual fun saveFramedPhoto(
        bytes: ByteArray, panX: Float, panY: Float, userScale: Float,
        viewW: Float, viewH: Float, filename: String
    ): String = ""

    actual fun cropAndReplacePhoto(
        sourcePath: String, relLeft: Float, relTop: Float,
        relRight: Float, relBottom: Float
    ): String = ""
}
