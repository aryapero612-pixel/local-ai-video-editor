package com.localai.videoeditor

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var clips by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                loading = true
                val newItems = uris
                    .filter { uri -> clips.none { it.uri == uri } }
                    .map { uri ->
                        try {
                            context.contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (e: Exception) {
                        }
                        loadVideoItem(context, uri)
                    }
                clips = clips + newItems
                loading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Header() }

            item {
                ImportCard(
                    loading = loading,
                    onImport = { picker.launch(arrayOf("video/*")) }
                )
            }

            item {
                Text(
                    text = "Imported clips (${clips.size})",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (clips.isEmpty()) {
                item { EmptyState() }
            } else {
                items(clips, key = { it.uri.toString() }) { clip ->
                    ClipCard(
                        clip = clip,
                        onRemove = { clips = clips.filter { it.uri != clip.uri } }
                    )
                }
            }

            item {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Open editor (coming in Phase 2)")
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF7C5CFF), Color(0xFF22D3EE))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "AI Video Editor",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Local. Private. Yours.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ImportCard(loading: Boolean, onImport: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF4B2FD6), Color(0xFF0E7490))
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "Start a new project",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Import one or more videos. Your original files are never modified.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onImport,
                enabled = !loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0B0F1A)
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import videos", fontWeight = FontWeight.SemiBold)
            }
            if (loading) {
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "No clips yet. Tap \"Import videos\" to add some.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(28.dp)
        )
    }
}

@Composable
private fun ClipCard(clip: VideoItem, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (clip.thumbnail != null) {
                    Image(
                        bitmap = clip.thumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = clip.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = clipDetails(clip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remove from list",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun clipDetails(clip: VideoItem): String {
    val parts = mutableListOf<String>()
    if (clip.width > 0 && clip.height > 0) parts.add("${clip.width}x${clip.height}")
    if (clip.durationMs > 0) parts.add(formatDuration(clip.durationMs))
    if (clip.sizeBytes > 0) parts.add(formatSize(clip.sizeBytes))
    return if (parts.isEmpty()) "Details unavailable" else parts.joinToString("  •  ")
}

private fun formatDuration(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%d:%02d", m, s)
    }
}

private fun formatSize(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024) {
        String.format("%.2f GB", mb / 1024)
    } else {
        String.format("%.1f MB", mb)
    }
}
