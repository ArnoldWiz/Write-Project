package com.chear.planit.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    var isPaused = false
        private set

    fun startRecording(): File? {
        try {
            val file = FileUtils.createAudioFile(context)
            currentFile = file

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(file.absolutePath)

                try {
                    prepare()
                    start()
                    isPaused = false
                } catch (e: IOException) {
                    Log.e("AudioRecorder", "Error preparing/starting recorder: ${e.message}")
                    e.printStackTrace()
                    return null
                } catch (e: IllegalStateException) {
                    Log.e("AudioRecorder", "Illegal state in recorder: ${e.message}")
                    e.printStackTrace()
                    return null
                }
            }
            return file
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error creating file or recorder: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                recorder?.pause()
                isPaused = true
            } catch (e: IllegalStateException) {
                Log.e("AudioRecorder", "Error pausing recorder: ${e.message}")
            }
        }
    }

    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                recorder?.resume()
                isPaused = false
            } catch (e: IllegalStateException) {
                Log.e("AudioRecorder", "Error resuming recorder: ${e.message}")
            }
        }
    }

    fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                release()
            } catch (e: RuntimeException) {
                Log.e("AudioRecorder", "Error stopping recorder: ${e.message}")
                e.printStackTrace()
            }
        }
        recorder = null
        isPaused = false
    }
}
