package com.localai.videoeditor

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VideoItem(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val thumbnail: ImageBitmap?
)

// Only READS the video. The original file is never modified.
suspend fun loadVideoItem(context: Context, uri: Uri): VideoItem =
    withContext(Dispatchers.IO) {
        var name = "Video"
        var size = 0L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0) name = c.getString(nameIdx) ?: name
                    if (sizeIdx >= 0 && !c.isNull(sizeIdx)) size = c.getLong(sizeIdx)
                }
            }
        } catch (e: Exception) {
        }

        var duration = 0L
        var w = 0
        var h = 0
        var thumb: ImageBitmap? = null

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            duration = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            w = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: 0
            h = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: 0
            val rotation = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                ?.toIntOrNull() ?: 0
            if (rotation == 90 || rotation == 270) {
                val t = w
                w = h
                h = t
            }

            val frame = retriever.getFrameAtTime(
                1_000_000L,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            ) ?: retriever.getFrameAtTime(0L)

            if (frame != null) {
                val targetW = 320
                val targetH = (frame.height * (targetW.toFloat() / frame.width))
                    .toInt()
                    .coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(frame, targetW, targetH, true)
                thumb = scaled.asImageBitmap()
            }
        } catch (e: Exception) {
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
            }
        }

        VideoItem(
            uri = uri,
            name = name,
            sizeBytes = size,
            durationMs = duration,
            width = w,
            height = h,
            thumbnail = thumb
        )
    }
