package com.hichamdzz.translator.util

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

object AudioUtils {
    private const val SAMPLE_RATE = 16000
    private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
    private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT

    fun createAudioRecord(): AudioRecord {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
        return AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize)
    }

    suspend fun pcmToWav(pcmData: ByteArray, outputFile: File): File = withContext(Dispatchers.IO) {
        val totalDataLen = pcmData.size + 36
        val channels = 1
        val byteRate = SAMPLE_RATE * channels * 2
        FileOutputStream(outputFile).use { fos ->
            fos.write("RIFF".toByteArray())
            fos.write(intToByteArray(totalDataLen))
            fos.write("WAVE".toByteArray())
            fos.write("fmt ".toByteArray())
            fos.write(intToByteArray(16))
            fos.write(shortToByteArray(1))
            fos.write(shortToByteArray(channels.toShort()))
            fos.write(intToByteArray(SAMPLE_RATE))
            fos.write(intToByteArray(byteRate))
            fos.write(shortToByteArray((channels * 2).toShort()))
            fos.write(shortToByteArray(16))
            fos.write("data".toByteArray())
            fos.write(intToByteArray(pcmData.size))
            fos.write(pcmData)
        }
        outputFile
    }

    private fun intToByteArray(value: Int): ByteArray = byteArrayOf(
        (value and 0xff).toByte(), (value shr 8 and 0xff).toByte(),
        (value shr 16 and 0xff).toByte(), (value shr 24 and 0xff).toByte()
    )
    private fun shortToByteArray(value: Short): ByteArray = byteArrayOf(
        (value.toInt() and 0xff).toByte(), (value.toInt() shr 8 and 0xff).toByte()
    )
}
