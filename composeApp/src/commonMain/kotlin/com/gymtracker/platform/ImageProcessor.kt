package com.gymtracker.platform

expect class ImageProcessor(context: PlatformContext) {

    fun saveRawBytes(bytes: ByteArray, filename: String): String

    fun saveFramedPhoto(
        bytes: ByteArray,
        panX: Float, panY: Float, userScale: Float,
        viewW: Float, viewH: Float,
        filename: String
    ): String

    fun cropAndReplacePhoto(
        sourcePath: String,
        relLeft: Float, relTop: Float, relRight: Float, relBottom: Float
    ): String
}
