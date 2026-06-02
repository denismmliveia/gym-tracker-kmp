package com.gymtracker.platform

import com.gymtracker.domain.voice.VoiceResult
import kotlinx.coroutines.flow.Flow

expect class VoiceRecognizer(context: PlatformContext) {
    fun listen(): Flow<VoiceResult>
}
