package com.traxxiontech.syncplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.traxxiontech.syncplayer.model.LyricLine
import com.traxxiontech.syncplayer.model.SongItem
import com.traxxiontech.syncplayer.model.SyncedLyrics
import com.traxxiontech.syncplayer.service.PlaybackState
import com.traxxiontech.syncplayer.service.RepeatMode
import com.traxxiontech.syncplayer.ui.components.EqualizerDialog
import com.traxxiontech.syncplayer.ui.components.SkeuomorphicButton
import com.traxxiontech.syncplayer.ui.components.SkeuomorphicKnob
import com.traxxiontech.syncplayer.ui.components.SkeuomorphicSlider
import com.traxxiontech.syncplayer.ui.components.SoundWaveSeeker
import com.traxxiontech.syncplayer.ui.components.WaveformVisualizer
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors

enum class PlayerViewMode {
    COVER, LYRICS, SPLIT
}

@Composable
fun FullMusicPlayerScreen(
    playbackState: PlaybackState,
    syncedLyrics: SyncedLyrics?,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onSetBassBoost: (Short) -> Unit,
    onSaveLyrics: (Long, String) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong = playbackState.currentSong ?: return
    val glassColors = LocalGlassmorphicColors.current

    var viewMode by remember { mutableStateOf(PlayerViewMode.SPLIT) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showLyricsEditorDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    var bassLevel by remember { mutableFloatStateOf(playbackState.bassBoostStrength / 1000f) }
    var midLevel by remember { mutableFloatStateOf(0.5f) }
    var trebleLevel by remember { mutableFloatStateOf(0.5f) }

    val lyricsListState = rememberLazyListState()
    val lines = syncedLyrics?.lines ?: emptyList()

    val activeIndex by remember(playbackState.currentPositionMs, lines) {
        derivedStateOf {
            if (lines.isEmpty()) -1
            else {
                var found = -1
                for (i in lines.indices) {
                    if (playbackState.currentPositionMs >= lines[i].timestampMs) {
                        found = i
                    } else {
                        break
                    }
                }
                found
            }
        }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex in lines.indices && (viewMode == PlayerViewMode.LYRICS || viewMode == PlayerViewMode.SPLIT)) {
            lyricsListState.animateScrollToItem(
                index = (activeIndex - 1).coerceAtLeast(0),
                scrollOffset = 0
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(glassColors.backgroundGradient))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("full_music_player_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("player_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Collapse",
                        tint = Color.White
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(glassColors.surfaceGlass)
                        .border(1.dp, glassColors.borderGlass.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(3.dp)
                ) {
                    ViewModeChip(
                        title = "Art",
                        isSelected = viewMode == PlayerViewMode.COVER,
                        onClick = { viewMode = PlayerViewMode.COVER }
                    )
                    ViewModeChip(
                        title = "Synced",
                        isSelected = viewMode == PlayerViewMode.SPLIT,
                        onClick = { viewMode = PlayerViewMode.SPLIT }
                    )
                    ViewModeChip(
                        title = "Lyrics",
                        isSelected = viewMode == PlayerViewMode.LYRICS,
                        onClick = { viewMode = PlayerViewMode.LYRICS }
                    )
                }

                Row {
                    IconButton(onClick = { showEqualizerDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Equalizer,
                            contentDescription = "Equalizer",
                            tint = glassColors.glowColor
                        )
                    }
                    IconButton(onClick = { showSleepTimerDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (playbackState.sleepTimerMinutesLeft > 0) glassColors.glowColor else Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Center Display: Cover Art and/or Synced Lyrics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (viewMode) {
                    PlayerViewMode.COVER -> {
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .border(2.dp, glassColors.glowColor.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                                .background(glassColors.surfaceGlassSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = currentSong.artworkUri,
                                contentDescription = "Full Album Art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    PlayerViewMode.SPLIT -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.5.dp, glassColors.glowColor, RoundedCornerShape(16.dp))
                                        .background(glassColors.surfaceGlassSecondary)
                                ) {
                                    AsyncImage(
                                        model = currentSong.artworkUri,
                                        contentDescription = "Cover",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentSong.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = currentSong.artist,
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Synced Lyrics Active ⚡",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = glassColors.glowColor
                                    )
                                }
                                IconButton(onClick = { showLyricsEditorDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.FormatQuote,
                                        contentDescription = "Edit Lyrics",
                                        tint = glassColors.glowColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            SyncedLyricsContainer(
                                lines = lines,
                                activeIndex = activeIndex,
                                onLineClick = { timestamp -> onSeekTo(timestamp) },
                                listState = lyricsListState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }

                    PlayerViewMode.LYRICS -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Synchronized Lyrics",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = glassColors.glowColor
                                )
                                Text(
                                    text = "Tap line to seek",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            SyncedLyricsContainer(
                                lines = lines,
                                activeIndex = activeIndex,
                                onLineClick = { timestamp -> onSeekTo(timestamp) },
                                listState = lyricsListState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Song Info & Waveform
            if (viewMode == PlayerViewMode.COVER) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentSong.artist} • ${currentSong.album}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { onToggleFavorite(currentSong.id) },
                        modifier = Modifier.testTag("player_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (currentSong.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (currentSong.isFavorite) Color(0xFFFF0055) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                WaveformVisualizer(
                    isPlaying = playbackState.isPlaying,
                    barCount = 36,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sound Wave Progress Format with Peak Detection
            SoundWaveSeeker(
                progressMs = playbackState.currentPositionMs,
                durationMs = playbackState.durationMs,
                isPlaying = playbackState.isPlaying,
                songId = currentSong.id,
                onSeekTo = onSeekTo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.testTag("player_shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.isShuffle) glassColors.glowColor else Color.White.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(glassColors.glowColor.copy(alpha = 0.4f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SkeuomorphicButton(
                        onClick = onPlayPause,
                        shape = CircleShape,
                        glowing = true,
                        modifier = Modifier
                            .size(60.dp)
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.testTag("player_repeat_button")
                ) {
                    Icon(
                        imageVector = if (playbackState.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        tint = if (playbackState.repeatMode != RepeatMode.OFF) glassColors.glowColor else Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speed: ${playbackState.playbackSpeed}x",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = glassColors.glowColor,
                    modifier = Modifier.clickable { showSpeedDialog = true }
                )

                if (playbackState.sleepTimerMinutesLeft > 0) {
                    Text(
                        text = "Sleep: ${playbackState.sleepTimerMinutesLeft}m left",
                        fontSize = 12.sp,
                        color = Color(0xFFFF9E00)
                    )
                }
            }
        }
    }

    // Equalizer Dialog
    EqualizerDialog(
        isOpen = showEqualizerDialog,
        onDismiss = { showEqualizerDialog = false },
        initialBassBoost = playbackState.bassBoostStrength,
        onBassBoostChanged = { strength ->
            onSetBassBoost(strength)
        },
        onBandsChanged = { bands ->
            // Band gains applied
        }
    )

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        val timerOptions = listOf(0, 15, 30, 45, 60, 90)
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            confirmButton = {},
            title = {
                Text("Sleep Timer", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    timerOptions.forEach { minutes ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetSleepTimer(minutes)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (minutes == 0) "Turn Off Timer" else "$minutes minutes",
                                color = if (playbackState.sleepTimerMinutesLeft == minutes) glassColors.glowColor else Color.White,
                                fontWeight = if (playbackState.sleepTimerMinutesLeft == minutes) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFF131722)
        )
    }

    // Speed Dialog
    if (showSpeedDialog) {
        val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            confirmButton = {},
            title = {
                Text("Playback Speed", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    speedOptions.forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (playbackState.playbackSpeed == speed) glassColors.glowColor else Color.White,
                                fontWeight = if (playbackState.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFF131722)
        )
    }

    // Lyrics Editor Dialog
    if (showLyricsEditorDialog) {
        var rawLrcText by remember {
            mutableStateOf(
                lines.joinToString("\n") { line ->
                    val min = line.timestampMs / 60000
                    val sec = (line.timestampMs % 60000) / 1000
                    val cs = (line.timestampMs % 1000) / 10
                    "[%02d:%02d.%02d]%s".format(min, sec, cs, line.text)
                }
            )
        }

        AlertDialog(
            onDismissRequest = { showLyricsEditorDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveLyrics(currentSong.id, rawLrcText)
                        showLyricsEditorDialog = false
                    }
                ) {
                    Text("Save Lyrics", color = glassColors.glowColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLyricsEditorDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            },
            title = {
                Text("Song Lyrics (LRC Format)", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    Text(
                        text = "Edit or paste timestamped lyrics ([mm:ss.xx] Lyric line)",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rawLrcText,
                        onValueChange = { rawLrcText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = glassColors.glowColor,
                            unfocusedBorderColor = glassColors.borderGlass,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            containerColor = Color(0xFF131722)
        )
    }
}

@Composable
fun SyncedLyricsContainer(
    lines: List<LyricLine>,
    activeIndex: Int,
    onLineClick: (Long) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassmorphicColors.current

    if (lines.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No lyrics loaded.\nTap the quote icon above to paste or load an .lrc file.",
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(lines) { index, line ->
                val isActive = index == activeIndex
                val isPast = index < activeIndex

                val textColor = when {
                    isActive -> glassColors.glowColor
                    isPast -> Color.White.copy(alpha = 0.75f)
                    else -> Color.White.copy(alpha = 0.35f)
                }

                val scale = if (isActive) 1.06f else 1.0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLineClick(line.timestampMs) }
                        .background(
                            if (isActive) glassColors.surfaceGlassSecondary.copy(alpha = 0.5f) else Color.Transparent
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = line.text,
                        fontSize = if (isActive) 18.sp else 15.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale)
                    )
                }
            }
        }
    }
}

@Composable
fun ViewModeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) glassColors.surfaceGlassSecondary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) glassColors.glowColor else Color.White.copy(alpha = 0.6f)
        )
    }
}

fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return "%d:%02d".format(minutes, seconds)
}
