package com.traxxiontech.syncplayer.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import com.traxxiontech.syncplayer.util.QrCodeGenerator
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traxxiontech.syncplayer.model.PhotoItem
import com.traxxiontech.syncplayer.model.SongItem
import com.traxxiontech.syncplayer.model.UserProfile
import com.traxxiontech.syncplayer.model.VideoItem
import com.traxxiontech.syncplayer.ui.components.FrostedGlassCard
import com.traxxiontech.syncplayer.ui.components.SkeuomorphicButton
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors

@Composable
fun MediaShareScreen(
    userProfile: UserProfile,
    songs: List<SongItem>,
    videos: List<VideoItem>,
    photos: List<PhotoItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glassColors = LocalGlassmorphicColors.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Fast Share Media, 1: QR & Hotspot Pairing
    var showQrDialog by remember { mutableStateOf(false) }

    val selectedUris = remember { mutableStateListOf<Uri>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("media_share_screen")
    ) {
        // Share Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Offline Media Share 🔄",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Direct transfer via Quick Share, Wi-Fi Direct & Bluetooth",
                    fontSize = 12.sp,
                    color = glassColors.glowColor
                )
            }

            IconButton(
                onClick = { showQrDialog = true },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(glassColors.surfaceGlass)
                    .border(1.dp, glassColors.glowColor, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode,
                    contentDescription = "QR Code",
                    tint = glassColors.glowColor
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = glassColors.glowColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = glassColors.glowColor
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Select & Send", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Device Pairing", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${selectedUris.size} items selected",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Send over local Wi-Fi or Bluetooth",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }

                                SkeuomorphicButton(
                                    onClick = {
                                        if (selectedUris.isNotEmpty()) {
                                            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                                type = "*/*"
                                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(selectedUris))
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Send Media Offline"))
                                        }
                                    },
                                    glowing = selectedUris.isNotEmpty()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = "Send",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Send Media", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Songs
                    if (songs.isNotEmpty()) {
                        item {
                            Text(
                                text = "🎵 Music Tracks (${songs.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(songs) { song ->
                            val isSelected = selectedUris.contains(song.uri)
                            FrostedGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedUris.remove(song.uri)
                                        else selectedUris.add(song.uri)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (it) selectedUris.add(song.uri) else selectedUris.remove(song.uri)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = glassColors.glowColor,
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = song.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text(text = "${song.artist} • ${song.durationFormatted}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }

                    // Videos
                    if (videos.isNotEmpty()) {
                        item {
                            Text(
                                text = "🎥 Video Files (${videos.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(videos) { video ->
                            val isSelected = selectedUris.contains(video.uri)
                            FrostedGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedUris.remove(video.uri)
                                        else selectedUris.add(video.uri)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (it) selectedUris.add(video.uri) else selectedUris.remove(video.uri)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = glassColors.glowColor,
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = video.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text(text = "${video.resolution} • ${video.sizeFormatted}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Device Pairing Information
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(glassColors.surfaceGlassSecondary)
                            .border(2.dp, glassColors.glowColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NearMe,
                            contentDescription = "Radar",
                            tint = glassColors.glowColor,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Ready to Receive Media",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sync Player utilizes Android's zero-friction offline sharing subsystem. Use Quick Share or Wi-Fi Direct from any sending phone or tablet.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "This Device Identity", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("Device Name", userProfile.fullName.ifBlank { "Sync Player Device" })
                            DetailRow("Protocol", "Wi-Fi Direct / Hotspot")
                            DetailRow("Security", "WPA3 / End-to-End P2P")
                        }
                    }
                }
            }
        }
    }

    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Close", color = glassColors.glowColor)
                }
            },
            title = {
                Text("Sync Share Connection QR", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan this code with another device to establish an offline direct link.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val qrPayload = remember(userProfile) {
                        "WIFI:S:SyncPlayer-${userProfile.username.ifBlank { "Direct" }};T:WPA;P:SyncShare2026;H:false;;"
                    }
                    val qrBitmap = remember(qrPayload) {
                        QrCodeGenerator.encodeToBitmap(qrPayload, 512)
                    }

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Authentic Scannable QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SSID: SyncPlayer-${userProfile.username.ifBlank { "Direct" }}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.glowColor
                    )
                    Text(
                        text = "Scannable with any Camera or Wi-Fi Scanner",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            },
            containerColor = Color(0xFF131722)
        )
    }
}
