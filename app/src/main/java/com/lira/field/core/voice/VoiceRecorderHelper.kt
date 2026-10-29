package com.lira.field.core.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class VoiceRecorderHelper(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    var isRecording: Boolean = false
        private set

    fun startRecording(reportId: String): Result<File> {
        if (isRecording) {
            stopRecording()
        }

        val dir = File(context.filesDir, "voice_reports").apply { if (!exists()) mkdirs() }
        val file = File(dir, "report_${reportId}.m4a")
        currentFile = file

        return try {
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(64000)
                setAudioChannels(1)
                setMaxDuration(60000) // 60s max
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            isRecording = true
            Result.success(file)
        } catch (e: Exception) {
            isRecording = false
            currentFile = null
            Result.failure(e)
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return currentFile
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false
            currentFile
        } catch (e: Exception) {
            recorder = null
            isRecording = false
            currentFile
        }
    }

    fun cancelRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        recorder = null
        isRecording = false
        currentFile?.delete()
        currentFile = null
    }
}

