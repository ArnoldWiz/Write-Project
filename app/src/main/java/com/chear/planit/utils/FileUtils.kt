package com.chear.planit.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    fun createImageFile(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir("Pictures") ?: context.filesDir
        if (!storageDir.exists()) storageDir.mkdirs() // Asegurar que el directorio existe
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            image
        )
    }

    fun createAudioFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFileName = "AUDIO_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir("Music") ?: context.filesDir
        if (!storageDir.exists()) storageDir.mkdirs() // Asegurar que el directorio existe
        return File.createTempFile(
            audioFileName,
            ".3gp",
            storageDir
        )
    }

    fun createVideoFile(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VIDEO_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir("Movies") ?: context.filesDir
        if (!storageDir.exists()) storageDir.mkdirs() // Asegurar que el directorio existe
        val video = File.createTempFile(
            videoFileName,
            ".mp4",
            storageDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            video
        )
    }
}
