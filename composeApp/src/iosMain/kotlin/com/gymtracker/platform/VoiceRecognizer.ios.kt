package com.gymtracker.platform

import com.gymtracker.domain.voice.VoiceResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Foundation.NSLocale
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus

actual class VoiceRecognizer actual constructor(private val context: PlatformContext) {

    actual fun listen(): Flow<VoiceResult> = callbackFlow {
        val locale = NSLocale(localeIdentifier = "es-ES")
        val recognizer = SFSpeechRecognizer(locale = locale)

        SFSpeechRecognizer.requestAuthorization { status ->
            if (status != SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) {
                trySend(VoiceResult.Error("Permiso de reconocimiento de voz denegado"))
                close()
                return@requestAuthorization
            }

            val audioEngine = AVAudioEngine()
            val request = SFSpeechAudioBufferRecognitionRequest()
            request.shouldReportPartialResults = false

            val inputNode = audioEngine.inputNode
            val format = inputNode.outputFormatForBus(0u)
            inputNode.installTapOnBus(0u, bufferSize = 1024u, format = format) { buffer, _ ->
                buffer?.let { request.appendAudioPCMBuffer(it) }
            }

            val session = AVAudioSession.sharedInstance()
            session.setCategory(
                AVAudioSessionCategoryRecord,
                mode = AVAudioSessionModeMeasurement,
                options = 0u,
                error = null
            )
            session.setActive(true, error = null)

            runCatching { audioEngine.startAndReturnError(null) }

            recognizer?.recognitionTaskWithRequest(request) { result, error ->
                if (error != null) {
                    audioEngine.stop()
                    inputNode.removeTapOnBus(0u)
                    trySend(VoiceResult.Error(error.localizedDescription))
                    close()
                } else if (result?.isFinal == true) {
                    audioEngine.stop()
                    inputNode.removeTapOnBus(0u)
                    val text = result.bestTranscription.formattedString
                    trySend(VoiceResult.Success(text))
                    close()
                }
            }
        }

        awaitClose { }
    }
}
