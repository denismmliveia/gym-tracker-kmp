package com.gymtracker.platform

import com.gymtracker.domain.voice.VoiceResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class VoiceRecognizer actual constructor(private val context: PlatformContext) {
    actual fun listen(): Flow<VoiceResult> =
        flowOf(VoiceResult.Error("Voice not implemented on iOS yet"))
}
