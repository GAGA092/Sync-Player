package com.traxxiontech.syncplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traxxiontech.syncplayer.model.PhotoItem
import com.traxxiontech.syncplayer.model.SmartScanReport
import com.traxxiontech.syncplayer.model.SongItem
import com.traxxiontech.syncplayer.model.VideoItem
import com.traxxiontech.syncplayer.ui.components.FrostedGlassCard
import com.traxxiontech.syncplayer.ui.components.SkeuomorphicButton
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors

@Composable
fun MediaManagerScreen(
    songs: List<SongItem>,
    videos: List<VideoItem>,
    photos: List<PhotoItem>,
    isScanning: Boolean,
    onRescan: () -> Unit,
    onRunSmartScan: () -> SmartScanReport,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassmorphicColors.current

    var scanReport by remember { mutableStateOf<SmartScanReport?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }
    var editingSong by remember { mutableStateOf<SongItem?>(null) }

    val totalAudioBytes = songs.sumOf { it.sizeBytes }
    val totalVideoBytes = videos.sumOf { it.sizeBytes }
    val totalPhotoBytes = photos.sumOf { it.sizeBytes }
    val totalMediaBytes = totalAudioBytes + totalVideoBytes + totalPhotoBytes

    val folders = remember(songs, videos) {
        val folderMap = mutableMapOf<String, Int>()
        songs.forEach {
            val f = if (it.path.contains('/')) it.path.substringBeforeLast('/').substringAfterLast('/') else "Music"
            folderMap[f] = (folderMap[f] ?: 0) + 1
        }
        videos.forEach {
            val f = if (it.path.contains('/')) it.path.substringBeforeLast('/').substringAfterLast('/') else "Movies"
            folderMap[f] = (folderMap[f] ?: 0) + 1
        }
        if (folderMap.isEmpty()) {
            folderMap["Download"] = 0
            folderMap["Music"] = songs.size
            folderMap["DCIM"] = photos.size
            folderMap["Movies"] = videos.size
        }
        folderMap.map { Pair(it.key, it.value) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("media_manager_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Media Manager & Storage 📂",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Organize folders, scan duplicates, and inspect storage",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = onRescan,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(glassColors.surfaceGlass)
                        .border(1.dp, glassColors.glowColor, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Rescan Media",
                        tint = glassColors.glowColor
                    )
                }
            }
        }

        // Storage Analyzer Card
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Storage,
                                contentDescription = "Storage",
                                tint = glassColors.glowColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Device Media Storage",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }

                        Text(
                            text = formatStorage(totalMediaBytes),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = glassColors.glowColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(glassColors.skeuoShadow)
                    ) {
                        val audioRatio = if (totalMediaBytes > 0) (totalAudioBytes.toFloat() / totalMediaBytes).coerceIn(0.1f, 0.9f) else 0.33f
                        val videoRatio = if (totalMediaBytes > 0) (totalVideoBytes.toFloat() / totalMediaBytes).coerceIn(0.1f, 0.9f) else 0.33f

                        Box(
                            modifier = Modifier
                                .weight(audioRatio)
                                .fillMaxSize()
                                .background(Color(0xFF00E5FF))
                        )
                        Box(
                            modifier = Modifier
                                .weight(videoRatio)
                                .fillMaxSize()
                                .background(Color(0xFFFF007F))
                        )
                        Box(
                            modifier = Modifier
                                .weight((1f - audioRatio - videoRatio).coerceAtLeast(0.1f))
                                .fillMaxSize()
                                .background(Color(0xFFFFD700))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StorageLegend(color = Color(0xFF00E5FF), label = "Music", value = formatStorage(totalAudioBytes))
                        StorageLegend(color = Color(0xFFFF007F), label = "Videos", value = formatStorage(totalVideoBytes))
                        StorageLegend(color = Color(0xFFFFD700), label = "Photos", value = formatStorage(totalPhotoBytes))
                    }
                }
            }
        }

        // Smart Scanner Action
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Media Scanner ⚡",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Detect duplicates, corrupted files & missing artwork",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }

                    SkeuomorphicButton(
                        onClick = {
                            scanReport = onRunSmartScan()
                            showReportDialog = true
                        },
                        glowing = true
                    ) {
                        Text("Run Scan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }

        // Folder Browser
        item {
            Text(
                text = "Media Folders",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        items(folders) { (folderName, itemCount) ->
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Folder",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = folderName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "/storage/emulated/0/$folderName",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Text(
                        text = "$itemCount items",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.glowColor
                    )
                }
            }
        }

        if (songs.isNotEmpty()) {
            item {
                Text(
                    text = "Edit Song Tags",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            items(songs.take(3)) { song ->
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = song.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text(text = "${song.artist} • ${song.album}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        IconButton(onClick = { editingSong = song }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Tags", tint = glassColors.glowColor)
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog && scanReport != null) {
        val report = scanReport!!
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            confirmButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Done", color = glassColors.glowColor)
                }
            },
            title = {
                Text("Smart Scan Complete", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    DetailRow("Files Scanned", "${report.totalFilesScanned} items")
                    DetailRow("Duplicates Found", "${report.duplicateFiles.size} files")
                    DetailRow("Potential Storage Reclaim", formatStorage(report.potentialSavedBytes))
                    DetailRow("Missing Artwork", "${report.missingArtworkCount} songs")
                    DetailRow("Corrupted Media", "${report.corruptedFiles.size} items")
                }
            },
            containerColor = Color(0xFF131722)
        )
    }

    if (editingSong != null) {
        val s = editingSong!!
        var title by remember { mutableStateOf(s.title) }
        var artist by remember { mutableStateOf(s.artist) }
        var album by remember { mutableStateOf(s.album) }

        AlertDialog(
            onDismissRequest = { editingSong = null },
            confirmButton = {
                TextButton(onClick = { editingSong = null }) {
                    Text("Save Changes", color = glassColors.glowColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSong = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            },
            title = {
                Text("Edit Media Metadata", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Track Title") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = glassColors.glowColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = artist,
                        onValueChange = { artist = it },
                        label = { Text("Artist") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = glassColors.glowColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = album,
                        onValueChange = { album = it },
                        label = { Text("Album") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = glassColors.glowColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color(0xFF131722)
        )
    }
}

@Composable
fun StorageLegend(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

fun formatStorage(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024) "%.2f GB".format(mb / 1024.0) else "%.1f MB".format(mb)
}
