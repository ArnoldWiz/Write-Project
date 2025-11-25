package com.chear.planit.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@Composable
fun AttachmentItem(
    uriString: String,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val uri = remember(uriString) { uriString.toUri() }
    val fileName = remember(uri) { uri.lastPathSegment ?: "Archivo" }
    val mimeType = remember(uri) { context.contentResolver.getType(uri) }
    
    val isImage = mimeType?.startsWith("image") == true || fileName.endsWith(".jpg") || fileName.endsWith(".png")
    val isVideo = mimeType?.startsWith("video") == true || fileName.endsWith(".mp4")
    val isAudio = mimeType?.startsWith("audio") == true || fileName.endsWith(".3gp") || fileName.endsWith(".mp3")

    fun openFile() {
        try {
            var finalUri = uri
            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")
                finalUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }

            var finalMimeType = mimeType
            if (finalMimeType == null) {
                val extension = fileName.substringAfterLast('.', "")
                if (extension.isNotEmpty()) {
                    finalMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase(Locale.getDefault()))
                }
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(finalUri, finalMimeType ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir con"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openFile() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isImage -> {
                        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                        LaunchedEffect(uri) {
                            bitmap = loadThumbnail(context, uri)
                        }
                        
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = "Vista previa imagen",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Face, contentDescription = "Imagen")
                        }
                    }
                    isVideo -> {
                        var videoBitmap by remember { mutableStateOf<Bitmap?>(null) }
                        LaunchedEffect(uri) {
                            videoBitmap = loadVideoThumbnail(context, uri)
                        }

                        if (videoBitmap != null) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    bitmap = videoBitmap!!.asImageBitmap(),
                                    contentDescription = "Vista previa video",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Icon(
                                    Icons.Default.PlayArrow, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Video", modifier = Modifier.size(32.dp))
                        }
                    }
                    isAudio -> {
                        Icon(Icons.Default.Phone, contentDescription = "Audio", modifier = Modifier.size(32.dp))
                    }
                    else -> {
                        Icon(Icons.Default.List, contentDescription = "Archivo", modifier = Modifier.size(32.dp))
                    }
                }
            }

            Text(
                text = fileName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Clear, contentDescription = "Quitar adjunto")
            }
        }
    }
}

suspend fun loadThumbnail(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)

            context.contentResolver.openInputStream(uri)?.use { stream2 ->
                options.inSampleSize = calculateInSampleSize(options, 100, 100)
                options.inJustDecodeBounds = false
                BitmapFactory.decodeStream(stream2, null, options)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun loadVideoThumbnail(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, uri)
        retriever.getFrameAtTime()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        try {
            retriever.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
