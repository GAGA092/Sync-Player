package com.traxxiontech.syncplayer.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.traxxiontech.syncplayer.model.MediaType
import com.traxxiontech.syncplayer.model.PhotoItem
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors
import kotlinx.coroutines.delay

@Composable
fun PhotoGalleryScreen(
    photos: List<PhotoItem>,
    onPhotoClick: (PhotoItem) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onMoveToVault: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassmorphicColors.current
    val albums = remember(photos) {
        listOf("All Photos") + photos.map { it.albumName }.distinct()
    }
    var selectedAlbum by remember { mutableStateOf("All Photos") }

    val filteredPhotos = remember(photos, selectedAlbum) {
        if (selectedAlbum == "All Photos") photos
        else photos.filter { it.albumName == selectedAlbum }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("photo_gallery_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Photo Gallery 🖼️",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${filteredPhotos.size} photos on device",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(albums) { album ->
                val isSelected = selectedAlbum == album
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) glassColors.surfaceGlassSecondary else glassColors.surfaceGlass)
                        .border(
                            1.dp,
                            if (isSelected) glassColors.glowColor else glassColors.borderGlass.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedAlbum = album }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = album,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) glassColors.glowColor else Color.White
                    )
                }
            }
        }

        if (filteredPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = "No Photos",
                        tint = glassColors.glowColor,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No Photos Found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Photos captured on your device will automatically show up here.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPhotos) { photo ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(glassColors.surfaceGlassSecondary)
                            .border(1.dp, glassColors.borderGlass.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { onPhotoClick(photo) }
                    ) {
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (photo.isFavorite) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Favorite",
                                tint = Color(0xFFFF0055),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullscreenPhotoViewer(
    photo: PhotoItem,
    allPhotos: List<PhotoItem>,
    onClose: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onMoveToVault: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glassColors = LocalGlassmorphicColors.current

    var currentPhoto by remember { mutableStateOf(photo) }
    var scale by remember { mutableFloatStateOf(1f) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var isSlideshowActive by remember { mutableStateOf(false) }

    LaunchedEffect(isSlideshowActive, currentPhoto) {
        if (isSlideshowActive && allPhotos.isNotEmpty()) {
            delay(3500)
            val nextIdx = (allPhotos.indexOf(currentPhoto) + 1) % allPhotos.size
            currentPhoto = allPhotos[nextIdx]
            scale = 1f
            rotationAngle = 0f
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("fullscreen_photo_viewer")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.8f, 5f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = currentPhoto.uri,
                contentDescription = currentPhoto.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        rotationZ = rotationAngle
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                    )
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = currentPhoto.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1
            )

            Row {
                IconButton(onClick = { isSlideshowActive = !isSlideshowActive }) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = "Slideshow",
                        tint = if (isSlideshowActive) glassColors.glowColor else Color.White
                    )
                }

                IconButton(onClick = { showDetailsDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info",
                        tint = Color.White
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { rotationAngle = (rotationAngle + 90f) % 360f }) {
                Icon(
                    imageVector = Icons.Filled.RotateRight,
                    contentDescription = "Rotate",
                    tint = Color.White
                )
            }

            IconButton(onClick = {
                onToggleFavorite(currentPhoto.id)
                currentPhoto = currentPhoto.copy(isFavorite = !currentPhoto.isFavorite)
            }) {
                Icon(
                    imageVector = if (currentPhoto.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (currentPhoto.isFavorite) Color(0xFFFF0055) else Color.White
                )
            }

            IconButton(onClick = {
                onMoveToVault(currentPhoto)
                onClose()
            }) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Lock in Vault",
                    tint = glassColors.glowColor
                )
            }

            IconButton(onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, currentPhoto.uri)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Photo via Sync Player"))
            }) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }
    }

    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close", color = glassColors.glowColor)
                }
            },
            title = {
                Text("Photo Metadata & EXIF", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    DetailRow("File Name", currentPhoto.title)
                    DetailRow("Resolution", "${currentPhoto.width} × ${currentPhoto.height}")
                    DetailRow("File Size", currentPhoto.sizeFormatted)
                    DetailRow("Album", currentPhoto.albumName)
                    DetailRow("Path", currentPhoto.path.ifBlank { "Internal Device Storage" })
                }
            },
            containerColor = Color(0xFF131722)
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
