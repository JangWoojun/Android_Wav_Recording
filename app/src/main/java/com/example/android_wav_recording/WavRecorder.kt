package com.example.android_wav_recording

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavRecorder(private val outputFile: File) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    fun startRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        Thread {
            writeAudioDataToFile()
        }.start()
    }

    fun stopRecording(): File {
        isRecording = false
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null

        return convertPcmToWav(outputFile)
    }

    private fun writeAudioDataToFile() {
        val pcmFile = File(outputFile.absolutePath.replace(".wav", ".pcm"))
        val outputStream = FileOutputStream(pcmFile)
        val buffer = ByteArray(bufferSize)

        while (isRecording) {
            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (read > 0) {
                outputStream.write(buffer, 0, read)
            }
        }

        outputStream.close()
    }

    private fun convertPcmToWav(wavFile: File): File {
        val pcmFile = File(wavFile.absolutePath.replace(".wav", ".pcm"))
        val pcmData = pcmFile.readBytes()
        val wavData = addWavHeader(pcmData)

        FileOutputStream(wavFile).use { it.write(wavData) }

        pcmFile.delete()
        return wavFile
    }

    private fun addWavHeader(pcmData: ByteArray): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 2

        val header = ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(sampleRate)
            putInt(byteRate)
            putShort(2)
            putShort(16)
            put("data".toByteArray())
            putInt(totalAudioLen)
        }.array()

        return header + pcmData
    }
}
